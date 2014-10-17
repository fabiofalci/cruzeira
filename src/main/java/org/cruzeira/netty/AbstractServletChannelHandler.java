/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.netty;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.DefaultCookie;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.ServerCookieEncoder;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.cruzeira.WebContext;
import org.cruzeira.ServerManager;
import org.cruzeira.servlet.HttpSession1;
import org.cruzeira.servlet.ServletRequest1;
import org.cruzeira.servlet.ServletResponse1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static io.netty.handler.codec.http.HttpHeaders.Names.SET_COOKIE;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * A generic ChannelHandler with some common methods related to http request
 * transformation, servlet, http session, etc.
 */
public abstract class AbstractServletChannelHandler extends ChannelInboundHandlerAdapter {

    protected ServerManager serverManager;
    private Logger logger = LoggerFactory.getLogger(getClass());

    public AbstractServletChannelHandler(ServerManager serverManager) {
        this.serverManager = serverManager;
    }

    protected ServletOutput doServlet(ChannelHandlerContext ctx, FullHttpRequest request, StringBuilder buf) throws Exception {
        ServletContext servletContext = getSpringContext().getServletContext();
        HttpSession1 httpSession = createHttpSession(request, servletContext);

        String method = request.getMethod().name();
        byte[] array = null;
        if (request.content().hasArray()) {
            array = request.content().array();
        }
        String contentType = request.headers().get("Content-type");

        ServletRequest1 servletRequest = new ServletRequest1(request.getUri(), getSpringContext(), method, array, contentType, httpSession);
        buildServletRequestHeader(servletRequest, request);

        return doServlet(ctx, buf, servletRequest, new ServletResponse1());
    }

    public void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status);
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.content().setBytes(0, Unpooled.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));

        ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    protected ServletOutput doServlet(ChannelHandlerContext ctx, StringBuilder buf, ServletRequest1 servletRequest, ServletResponse1 servletResponse)
            throws Exception {
        getDispatcherServlet().service(servletRequest, servletResponse);

        if (servletResponse.isError()) {
            logger.info("server error");
            sendError(ctx, HttpResponseStatus.valueOf(servletResponse.getStatus()));
            return null;
        }
        if (isCheckAsync() && servletRequest.isAsync()) {
            return new ServletOutput(servletRequest, servletResponse, true);
        }

        if (servletResponse.getStringWriter() != null) {
            buf.append(servletResponse.getStringWriter().getBuffer().toString());
            servletResponse.flushBuffer();
        }
        return new ServletOutput(servletRequest, servletResponse, false);
    }

    private void buildServletRequestHeader(ServletRequest1 servletRequest, FullHttpRequest httpRequest) {
        try {
            for (String name : httpRequest.headers().names()) {
                List<String> values = httpRequest.headers().getAll(name);
                for (String value : values) {
                    servletRequest.addHeader(name, value);
                    logger.info("Request Header: " + name + ", Value: " + value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected boolean isCheckAsync() {
        return true;
    }

    protected HttpSession1 createHttpSession(FullHttpRequest request, ServletContext servletContext) {
        HttpSession1 httpSession = null;
        try {
            httpSession = new HttpSession1(servletContext);
            String cookieString = request.headers().get(COOKIE);
            if (StringUtils.isNotBlank(cookieString)) {
                Set<Cookie> cookies = CookieDecoder.decode(cookieString);
                if (!cookies.isEmpty()) {
                    for (Cookie cookie : cookies) {
                        httpSession.setAttribute(cookie.getName(), cookie.getValue());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return httpSession;
    }

    protected void writeResponse(ChannelHandlerContext ctx, StringBuilder buf, ServletRequest1 servletRequest, ServletResponse1 servletResponse) {
        // Decide whether to close the connection or not.
//		boolean keepAlive = isKeepAlive(request);

        FullHttpResponse response;
        if (servletResponse != null) {
            try {
                response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(servletResponse.getStatus()), Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));
                for (String header : servletResponse.getHeaderNames()) {
                    Collection<String> collection = servletResponse.getHeaders(header);
                    for (String element : collection) {
                        response.headers().add(header, element);
                        logger.info("Response Header: " + header + ", Value: " + element);
                    }
                }
                if (!response.headers().contains("Content-Type")) {
                    response.headers().add("Content-Type", servletResponse.getContentType());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        } else {
            response = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
        }

        // if (keepAlive) {
        // // Add 'Content-Length' header only for a keep-alive connection.
        // response.setHeader(CONTENT_LENGTH,
        // response.getContent().readableBytes());
        // // Add keep alive header as per:
        // // -
        // //
        // http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
        // response.setHeader(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        // }

        try {
            HttpSession1 httpSession = (HttpSession1) servletRequest.getSession();
            List<Cookie> cookies = new ArrayList<>();

            for (String name : httpSession.getAttributeNamesAsCollection()) {
                Object value = httpSession.getAttribute(name);
                if (ClassUtils.isPrimitiveOrWrapper(value.getClass()) || value instanceof String) {
                    cookies.add(new DefaultCookie(name, value.toString()));
                }
            }

            response.headers().add(SET_COOKIE, ServerCookieEncoder.encode(cookies));
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        logger.info("Writing response");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.info("Exception caught " + cause);
        cause.getCause().printStackTrace();

        Channel ch = ctx.channel();
        ch.close();
    }

    protected WebContext getSpringContext() {
        return serverManager.getSpringContext();
    }

    protected HttpServlet getDispatcherServlet() {
        return serverManager.getDispatcherServlet();
    }

    protected class ServletOutput {

        private ServletRequest1 servletRequest;
        private ServletResponse1 servletResponse;
        private boolean async;

        public ServletOutput(ServletRequest1 servletRequest, ServletResponse1 servletResponse, boolean async) {
            this.servletRequest = servletRequest;
            this.servletResponse = servletResponse;
            this.async = async;
        }

        public ServletRequest1 getServletRequest() {
            return servletRequest;
        }

        public ServletResponse1 getServletResponse() {
            return servletResponse;
        }

        public boolean isAsync() {
            return async;
        }
    }
}
