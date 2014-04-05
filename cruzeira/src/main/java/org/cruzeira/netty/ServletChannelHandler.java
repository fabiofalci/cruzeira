/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.netty;

import org.cruzeira.server.ServerManager;
import org.cruzeira.servlet.ServletRequest1;
import org.cruzeira.servlet.ServletResponse1;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;

/**
 * The main ChannelHandler of Cruzeira. It receives the HTTP Request, translate
 * it to a HTTP Servelt Request and then to Spring (and then response). It
 * doesn't handle asyncronous requests, it's an AsyncServer job. It doesn't
 * handle file resources requests, it's a FileServer job.
 * 
 * @see AsyncServletChannelHandler
 * @see ResourcesChannelHandler
 */
public class ServletChannelHandler extends AbstractServletChannelHandler {

//	private boolean readingChunks;
	final Logger logger = LoggerFactory.getLogger(ServletChannelHandler.class);
	
	public ServletChannelHandler(ServerManager serverManager) {
		super(serverManager);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) throws Exception {
		HttpRequest request = (HttpRequest) event.getMessage();
		
		// is100ContinueExpected(request);
		StringBuilder buf = new StringBuilder();

		Object[] servlets = null;
		try {
			servlets = doServlet(ctx, event, request, buf);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
			sendError(ctx, INTERNAL_SERVER_ERROR);
			return;
		}
		
		if (servlets == null) {
		} else if (servlets[2] == Boolean.TRUE) {
			ctx.sendUpstream(event);
		} else if (request.isChunked()) {
//			readingChunks = true;
		} else {
			writeResponse(event, request, buf, (ServletRequest1) servlets[0], (ServletResponse1) servlets[1]);
		}
	}

}
