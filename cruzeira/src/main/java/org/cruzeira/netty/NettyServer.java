/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.netty;

import org.cruzeira.server.ServerManager;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main ChannelHandler of Cruzeira. It receives the HTTP Request, translate
 * it to a HTTP Servelt Request and then to Spring (and then response). It
 * doesn't handle asyncronous requests, it's an AsyncServer job. It doesn't
 * handle file resources requests, it's a FileServer job.
 * 
 * @see AsyncServer
 * @see FileServer
 * @see ServletServe
 */
public class NettyServer extends ServletServer {

//	private boolean readingChunks;
	final Logger logger = LoggerFactory.getLogger(NettyServer.class);
	
	/**
	 * In development there is class loader reloading and a limited number of
	 * threads, actually, only one boss thread, one worker thread and one async
	 * thread. 
	 */
	private boolean devMode;

	public NettyServer(ServerManager serverManager, boolean devMode) {
		super(serverManager);
		this.devMode = devMode;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) throws Exception {
		if (devMode) {
			serverManager.beforeRequest();
		}
		HttpRequest request = (HttpRequest) event.getMessage();
		if (request.getUri().startsWith("/resources/")) {
			return;
		}

		// is100ContinueExpected(request);
		StringBuilder buf = new StringBuilder();

		Object[] servlets = doServlet(ctx, event, request, buf);
		if (servlets == null) {
		} else if (servlets[2] == Boolean.TRUE) {
			ctx.sendUpstream(event);
		} else if (request.isChunked()) {
//			readingChunks = true;
		} else {
			writeResponse(event, request, buf, servlets[0], servlets[1]);
		}
	}

}
