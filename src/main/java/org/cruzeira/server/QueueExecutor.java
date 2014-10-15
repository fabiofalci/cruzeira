/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.server;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.cruzeira.netty.ObjectLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.AsyncTaskExecutor;

public class QueueExecutor implements AsyncTaskExecutor {

    private Logger logger = LoggerFactory.getLogger(getClass());
    public static final ThreadLocal<Object> responses = new ThreadLocal<>();
    public static final ObjectLocal<Object> futures = new ObjectLocal<>();

    @Override
    public void execute(Runnable task) {
        logger.info("execute " + task);
    }

    @Override
    public void execute(Runnable task, long startTimeout) {
        logger.info("execute " + task + ", " + startTimeout);

    }

    @Override
    public Future<?> submit(Runnable task) {
        logger.info("submit runnable {}", task);
        FutureTask<Object> future = new FutureTask<>(task, null);
        Object response = responses.get();
        responses.remove();
        futures.set(response, future);
        return future;
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        logger.info("submit callable " + task);
        return null;
    }


}
