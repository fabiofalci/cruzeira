/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.cruzeira.WebContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;

/**
 * The main ChannelHandler of Cruzeira. It receives the HTTP Request, translate
 * it to a HTTP Servlet Request and then to Spring (and then response). It
 * doesn't handle asynchronous requests, it's an AsyncServer job.
 *
 * @see AsyncServletChannelHandler
 */
public class ServletChannelHandler extends AbstractServletChannelHandler {

    final Logger logger = LoggerFactory.getLogger(ServletChannelHandler.class);

    public ServletChannelHandler(WebContext webContext) {
        super(webContext);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        FullHttpRequest request = (FullHttpRequest) msg;
        // is100ContinueExpected(request);
        StringBuilder buf = new StringBuilder();

        ServletOutput servletOutput;
        try {
            servletOutput = doServlet(ctx, request, buf);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            sendError(ctx, INTERNAL_SERVER_ERROR);
            return;
        }

        if (servletOutput != null) {
            if (servletOutput.isAsync()) {
                // async - send  upstream
                ctx.fireChannelRead(servletOutput);
            } else {
                writeResponse(ctx, buf, servletOutput.getServletRequest(), servletOutput.getServletResponse());
            }
        }
    }

}
