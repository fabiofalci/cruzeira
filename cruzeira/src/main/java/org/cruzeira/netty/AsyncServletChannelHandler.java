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
 * <p/>
 * <p/>
 * There is a weird QueueExecutor with some nasty ThreadLocals to bring here the
 * Callable created in the Spring async controller to run in this thread. Maybe
 * think harder to come up a better solution.
 */
public class AsyncServletChannelHandler extends AbstractServletChannelHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public AsyncServletChannelHandler(ServerManager serverManager) {
        super(serverManager);
    }

    //    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        logger.info("Async message received");

        Object[] servlets = (Object[]) ctx.attr(STATE).get();
        StringBuilder buf = new StringBuilder();

        try {
            Runnable runnable = (Runnable) QueueExecutor.futures.get(servlets[1]);
            logger.info("Request, response, runnable: {}, {}, {}", servlets[0], servlets[1], runnable);
            QueueExecutor.futures.remove(servlets[1]);
            runnable.run();
            servlets = doServlet(ctx, buf, (ServletRequest1) servlets[0], (ServletResponse1) servlets[1]);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            sendError(ctx, INTERNAL_SERVER_ERROR);
            return;
        }

        if (servlets == null) {
//		} else if (request.isChunked()) {
//			// readingChunks = true;
        } else {
            writeResponse(ctx, request, buf, (ServletRequest1) servlets[0], (ServletResponse1) servlets[1]);
        }
    }

    @Override
    protected boolean isCheckAsync() {
        return false;
    }

}
