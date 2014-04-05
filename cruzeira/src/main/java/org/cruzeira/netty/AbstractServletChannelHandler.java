/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.netty;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.cruzeira.context.WebContext;
import org.cruzeira.server.ServerManager;
import org.cruzeira.servlet.HttpSession1;
import org.cruzeira.servlet.ServletRequest1;
import org.cruzeira.servlet.ServletResponse1;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * A generic ChannelHandler with some common methods related to http request
 * transformation, servlet, http session, etc.
 */
public class AbstractServletChannelHandler extends SimpleChannelHandler {

	protected ServerManager serverManager;
	private Logger logger = LoggerFactory.getLogger(getClass());
	public static final ChannelLocal<Object[]> data = new ChannelLocal<>();

	public AbstractServletChannelHandler(ServerManager serverManager) {
		this.serverManager = serverManager;
	}

	protected Object[] doServlet(ChannelHandlerContext ctx, MessageEvent event, HttpRequest request, StringBuilder buf) throws Exception {
        ServletContext servletContext = getSpringContext().getServletContext();
        HttpSession1 httpSession = createHttpSession(request, servletContext);

		String method = request.getMethod().getName();
		byte[] array = request.getContent().array();
		String contentType = request.getHeader("Content-type");

        ServletRequest1 servletRequest = new ServletRequest1(request.getUri(), getSpringContext(), method, array, contentType, httpSession);
		buildServletRequestHeader(servletRequest, request);

		return doServlet(ctx, event, buf, servletRequest, new ServletResponse1());
	}

	public void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
		response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
		response.setContent(ChannelBuffers.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));

		// Close the connection as soon as the error message is sent.
		ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
	}

	// array = servletRequest, servletResponse, async
	protected Object[] doServlet(ChannelHandlerContext ctx, MessageEvent event, StringBuilder buf, ServletRequest1 servletRequest, ServletResponse1 servletResponse)
			throws Exception {
        getDispatcherServlet().service(servletRequest, servletResponse);

		if (servletResponse.isError()) {
			logger.info("server error");
			sendError(ctx, HttpResponseStatus.valueOf(servletResponse.getStatus()));
			return null;
		}
		if (isCheckAsync() && servletRequest.isAsync()) {
			// logger.info("is async request");
			data.set(event.getChannel(), new Object[] { servletRequest, servletResponse });
			return new Object[] { servletRequest, servletResponse, true };
		}

		if (servletResponse.getStringWriter() != null) {
			buf.append(servletResponse.getStringWriter().getBuffer().toString());
			servletResponse.flushBuffer();
		}
		return new Object[] { servletRequest, servletResponse, false };
	}

	private void buildServletRequestHeader(ServletRequest1 servletRequest, HttpRequest httpRequest) {
		try {
			for (String name : httpRequest.getHeaderNames()) {
				List<String> values = httpRequest.getHeaders(name);
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

	protected HttpSession1 createHttpSession(HttpRequest request, ServletContext servletContext) {
		HttpSession1 httpSession = null;
		try {
			httpSession = new HttpSession1(servletContext);
			String cookieString = request.getHeader(COOKIE);
			if (StringUtils.isNotBlank(cookieString)) {
				CookieDecoder cookieDecoder = new CookieDecoder();
				Set<Cookie> cookies = cookieDecoder.decode(cookieString);
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

	protected void writeResponse(MessageEvent e, HttpRequest request, StringBuilder buf, ServletRequest1 servletRequest, ServletResponse1 servletResponse) {
		// Decide whether to close the connection or not.
//		boolean keepAlive = isKeepAlive(request);

		// Build the response object.
		HttpResponse response = null;
		if (servletResponse != null) {
			try {
				response = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(servletResponse.getStatus()));
				response.setContent(ChannelBuffers.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));
                for (String header : servletResponse.getHeaderNames()) {

					Collection<String> collection = servletResponse.getHeaders(header);
                    for (String element : collection) {
						response.addHeader(header, element);
						logger.info("Response Header: " + header + ", Value: " + element);
					}
				}
				if (!response.containsHeader("Content-Type")) {
					response.addHeader("Content-Type", servletResponse.getContentType());
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			response = new DefaultHttpResponse(HTTP_1_1, NOT_FOUND);
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

		// Encode the cookie.

		try {
			CookieEncoder cookieEncoder = new CookieEncoder(true);
            HttpSession1 httpSession = (HttpSession1) servletRequest.getSession();

            for (String name : httpSession.getAttributeNamesAsCollection()) {
				Object value = httpSession.getAttribute(name);
				if (ClassUtils.isPrimitiveOrWrapper(value.getClass()) || value instanceof String) {
					cookieEncoder.addCookie(name, value.toString());
					response.addHeader(SET_COOKIE, cookieEncoder.encode());
				}
			}

		} catch (Exception e1) {
			e1.printStackTrace();
		}

		// Write the response.
		logger.info("Writing response");
		ChannelFuture future = e.getChannel().write(response);

		// Close the non-keep-alive connection after the write operation is
		// done.
		// if (!keepAlive) {
		future.addListener(ChannelFutureListener.CLOSE);
		// }
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		logger.info("Exception caught " + e);
		e.getCause().printStackTrace();

		Channel ch = e.getChannel();
		ch.close();
	}

	protected WebContext getSpringContext() {
		return serverManager.getSpringContext();
	}

	protected HttpServlet getDispatcherServlet() {
		return serverManager.getDispatcherServlet();
	}
}
