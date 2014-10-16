/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.cruzeira.server.QueueExecutor;
import org.cruzeira.server.ServerManager;
import org.cruzeira.servlet.ServletRequest1;
import org.cruzeira.servlet.ServletResponse1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;

/**
 * This ChannelHandler handles Servlet async requests. It should not run on a
 * worker thread but in an async thread instead (a thread pool created specially
 * for async requests).
 *
 * There is a weird QueueExecutor with some nasty ThreadLocals to bring here the
 * Callable created in the Spring async controller to run in this thread. Maybe
 * think harder to come up a better solution.
 */
public class AsyncServletChannelHandler extends AbstractServletChannelHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public AsyncServletChannelHandler(ServerManager serverManager) {
        super(serverManager);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        logger.info("Async message received");

        ServletOutput servletOutput = (ServletOutput) msg;
        StringBuilder buf = new StringBuilder();

        try {
            Runnable runnable = (Runnable) QueueExecutor.futures.get(servletOutput.getServletResponse());
            logger.info("Request, response, runnable: {}, {}, {}", servletOutput.getServletRequest(), servletOutput.getServletResponse(), runnable);
            QueueExecutor.futures.remove(servletOutput.getServletResponse());
            runnable.run();
            servletOutput = doServlet(ctx, buf, servletOutput.getServletRequest(), servletOutput.getServletResponse());
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            sendError(ctx, INTERNAL_SERVER_ERROR);
            return;
        }

        if (servletOutput != null) {
            writeResponse(ctx, buf, servletOutput.getServletRequest(), servletOutput.getServletResponse());
        }
    }

    @Override
    protected boolean isCheckAsync() {
        return false;
    }

}
