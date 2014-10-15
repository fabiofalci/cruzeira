/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link javax.servlet.AsyncContext} that doesn't dispatch
 * itself, but just prepare the request. The real dispatch will happen in
 * another moment, for example, in a specialized Netty handler.
 */
public class AsyncContext1 implements AsyncContext {

    private ServletRequest servletRequest;
    private ServletResponse servletResponse;
    private long timeout;
    private List<AsyncListener> asyncListeners;
    final Logger logger = LoggerFactory.getLogger(AsyncContext1.class);

    public AsyncContext1(ServletRequest servletRequest, ServletResponse servletResponse) {
        Assert.notNull(servletRequest, "ServletRequest cannot be null in AsyncContext1");
        Assert.notNull(servletResponse, "ServletResponse cannot be null in AsyncContext1");
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
    }

    @Override
    public ServletRequest getRequest() {
        return servletRequest;
    }

    @Override
    public ServletResponse getResponse() {
        return servletResponse;
    }

    @Override
    public boolean hasOriginalRequestAndResponse() {
        return true;
    }

    @Override
    public void dispatch() {
        HttpServletRequest httpRequest = (HttpServletRequest) getRequest();
        String path = httpRequest.getRequestURI();
        dispatch(path);
    }

    @Override
    public void dispatch(String path) {
        dispatch(servletRequest.getServletContext(), path);
    }

    @Override
    public void dispatch(ServletContext context, String path) {
        logger.debug("Preparing HttpRequest for dispatching: {}", path);
        HttpServletRequest httpRequest = (HttpServletRequest) getRequest();
        servletRequest.setAttribute(ASYNC_REQUEST_URI, httpRequest.getRequestURI());
        servletRequest.setAttribute(ASYNC_CONTEXT_PATH, httpRequest.getContextPath());
        servletRequest.setAttribute(ASYNC_SERVLET_PATH, httpRequest.getServletPath());
        servletRequest.setAttribute(ASYNC_QUERY_STRING, httpRequest.getQueryString());
    }

    @Override
    public void complete() {
        try {
            servletResponse.flushBuffer();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        for (AsyncListener asyncListener : getAsyncListeners()) {
            try {
                asyncListener.onComplete(new AsyncEvent(this));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void start(Runnable run) {
        throw new UnsupportedOperationException();
    }

    private List<AsyncListener> getAsyncListeners() {
        if (asyncListeners == null) {
            asyncListeners = new ArrayList<>();
        }
        return asyncListeners;
    }

    @Override
    public void addListener(AsyncListener listener) {
        getAsyncListeners().add(listener);
    }

    @Override
    public void addListener(AsyncListener listener, ServletRequest servletRequest, ServletResponse servletResponse) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends AsyncListener> T createListener(Class<T> clazz) throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public long getTimeout() {
        return timeout;
    }

}
