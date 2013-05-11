/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.netty;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SET_COOKIE;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.cruzeira.context.WebContext;
import org.cruzeira.server.ServerManager;
import org.cruzeira.servlet.HttpSession1;
import org.cruzeira.servlet.ServletRequest1;
import org.cruzeira.servlet.ServletResponse1;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelLocal;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generic ChannelHandler with some common methods related to http request
 * transformation, servlet, http session, etc.
 * 
 * <p>
 * There are some strange methods here using a lot of reflection without no
 * apparent reason. The reason is, Cruzeira has a development mode that create a
 * brand new ClassLoader when user modify a Class. That way the user doesn't
 * need to stop and start the server, the application restart itself. It is not
 * the Netty server that restart but only the web application, so, after the
 * first restart, all Netty related code will use the original ClassLoader while
 * all Spring related code (the application) will use the new ClassLoader. That
 * is the reason, even the same Class from different ClassLoader, are different.
 * 
 */
public class ServletServer extends SimpleChannelHandler {

	protected ServerManager serverManager;
	private Logger logger = LoggerFactory.getLogger(getClass());
	public static final ChannelLocal<Object[]> data = new ChannelLocal<Object[]>();

	public ServletServer(ServerManager serverManager) {
		this.serverManager = serverManager;
	}

	protected Object[] doServlet(ChannelHandlerContext ctx, MessageEvent event, HttpRequest request, StringBuilder buf) {
		try {
			Class<?> stringClass = getClassLoader().loadClass("java.lang.String");
			Class<?> byteArrayClass = Array.newInstance(byte.class, 0).getClass();
			Class<?> webContextClass = getClassLoader().loadClass(WebContext.class.getName());
			Class<?> servletRequestClass = getClassLoader().loadClass(ServletRequest1.class.getName());
			Class<?> servletResponseClass = getClassLoader().loadClass(ServletResponse1.class.getName());

			Class<?> httpSessionClass = getClassLoader().loadClass("javax.servlet.http.HttpSession");

			Object springContext = getSpringContext();
			Object servletContext = springContext.getClass().getMethod("getServletContext").invoke(springContext);
			Object httpSession = createHttpSession(request, servletContext);

			Constructor<?> constructor = servletRequestClass.getConstructor(stringClass, webContextClass, stringClass, byteArrayClass,
					stringClass, httpSessionClass);
			String method = request.getMethod().getName();
			byte[] array = request.getContent().array();
			String contentType = request.getHeader("Content-type");
			Object servletRequest = constructor.newInstance(request.getUri(), getSpringContext(), method, array, contentType, httpSession);
			buildServletRequestHeader(servletRequest, request);
			Object servletResponse = servletResponseClass.newInstance();

			return doServlet(ctx, event, buf, servletRequest, servletResponse);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
		response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
		response.setContent(ChannelBuffers.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));

		// Close the connection as soon as the error message is sent.
		ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
	}

	// array = servletRequest, servletResponse, async
	protected Object[] doServlet(ChannelHandlerContext ctx, MessageEvent event, StringBuilder buf, Object servletRequest, Object servletResponse)
			throws Exception {
		Class<?> httpServletRequestClass = getClassLoader().loadClass("javax.servlet.ServletRequest");
		Class<?> httpServletResponseClass = getClassLoader().loadClass("javax.servlet.ServletResponse");
		Class<?> httpServletClass = getClassLoader().loadClass("javax.servlet.http.HttpServlet");
		httpServletClass.getMethod("service", httpServletRequestClass, httpServletResponseClass).invoke(getDispatcherServlet(), servletRequest,
				servletResponse);

		if ((boolean) servletResponse.getClass().getMethod("isError").invoke(servletResponse)) {
			logger.info("server error");
			int status = (int) servletResponse.getClass().getMethod("getStatus").invoke(servletResponse);
			sendError(ctx, HttpResponseStatus.valueOf(status));
			return null;
		}
		if (isCheckAsync() && (boolean) servletRequest.getClass().getMethod("isAsync").invoke(servletRequest)) {
			// logger.info("is async request");
			data.set(event.getChannel(), new Object[] { servletRequest, servletResponse });
			return new Object[] { servletRequest, servletResponse, true };
		}

		if (servletResponse.getClass().getMethod("getStringWriter").invoke(servletResponse) != null) {
			buf.append(((StringWriter) servletResponse.getClass().getMethod("getStringWriter").invoke(servletResponse)).getBuffer().toString());
			servletResponse.getClass().getMethod("flushBuffer").invoke(servletResponse);
		}
		return new Object[] { servletRequest, servletResponse, false };
	}

	private void buildServletRequestHeader(Object servletRequest, HttpRequest httpRequest) {
		try {
			Class<?> stringClass = getClassLoader().loadClass("java.lang.String");
			Class<?> objectClass = getClassLoader().loadClass("java.lang.Object");

			Method addHeaderMethod = servletRequest.getClass().getMethod("addHeader", stringClass, objectClass);
			for (String name : httpRequest.getHeaderNames()) {
				List<String> values = httpRequest.getHeaders(name);
				for (String value : values) {
					addHeaderMethod.invoke(servletRequest, name, value);
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

	protected Object createHttpSession(HttpRequest request, Object servletContext) {
		Object httpSession = null;
		try {
			Class<?> httpSessionClass = getClassLoader().loadClass(HttpSession1.class.getName());
			Class<?> servletContextClass = getClassLoader().loadClass("javax.servlet.ServletContext");

			Constructor<?> constructor = httpSessionClass.getConstructor(servletContextClass);
			httpSession = constructor.newInstance(servletContext);
			String cookieString = request.getHeader(COOKIE);
			if (StringUtils.isNotBlank(cookieString)) {
				CookieDecoder cookieDecoder = new CookieDecoder();
				Set<Cookie> cookies = cookieDecoder.decode(cookieString);
				if (!cookies.isEmpty()) {
					Method setAttribute = httpSession.getClass().getMethod("setAttribute", String.class, Object.class);
					for (Cookie cookie : cookies) {
						setAttribute.invoke(httpSession, cookie.getName(), cookie.getValue());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return httpSession;
	}

	protected void writeResponse(MessageEvent e, HttpRequest request, StringBuilder buf, Object servletRequest, Object servletResponse) {
		// Decide whether to close the connection or not.
//		boolean keepAlive = isKeepAlive(request);

		// Build the response object.
		HttpResponse response = null;
		if (servletResponse != null) {
			try {
				int status = (int) servletResponse.getClass().getMethod("getStatus").invoke(servletResponse);
				response = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(status));
				response.setContent(ChannelBuffers.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));
				Object headerNames = servletResponse.getClass().getMethod("getHeaderNames").invoke(servletResponse);
				Method getHeadersMethod = servletResponse.getClass().getMethod("getHeaders", String.class);

				Method getMethod = headerNames.getClass().getMethod("get", int.class);
				int size = (int) headerNames.getClass().getMethod("size").invoke(headerNames);
				for (int i = 0; i < size; i++) {
					Object obj = getMethod.invoke(headerNames, i);

					Object collection = getHeadersMethod.invoke(servletResponse, obj);
					int sizeValues = (int) collection.getClass().getMethod("size").invoke(collection);
					for (int j = 0; j < sizeValues; j++) {
						Object element = collection.getClass().getMethod("get", int.class).invoke(collection, j);
						response.addHeader((String) obj, element);
						logger.info("Response Header: " + obj + ", Value: " + element);
					}
				}
				if (!response.containsHeader("Content-Type")) {
					response.addHeader("Content-Type", servletResponse.getClass().getMethod("getContentType").invoke(servletResponse));
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
			Object httpSession = servletRequest.getClass().getMethod("getSession").invoke(servletRequest);
			Object attributeNames = httpSession.getClass().getMethod("getAttributeNamesAsCollection").invoke(httpSession);
			Method getAttribute = httpSession.getClass().getMethod("getAttribute", String.class);
			Method getMethod = attributeNames.getClass().getMethod("get", int.class);

			int size = (int) attributeNames.getClass().getMethod("size").invoke(attributeNames);
			for (int i = 0; i < size; i++) {
				Object name = getMethod.invoke(attributeNames, i);
				Object value = getAttribute.invoke(httpSession, name);
				if (ClassUtils.isPrimitiveOrWrapper(value.getClass()) || value instanceof String) {
					cookieEncoder.addCookie((String) name, value.toString());
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
		System.out.println("Exception caught");
		e.getCause().printStackTrace();

		Channel ch = e.getChannel();
		ch.close();
	}

	protected ClassLoader getClassLoader() {
		return serverManager.getClassLoader();
	}

	protected Object getSpringContext() {
		return serverManager.getSpringContext();
	}

	protected Object getDispatcherServlet() {
		return serverManager.getDispatcherServlet();
	}
}
