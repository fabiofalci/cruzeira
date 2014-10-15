/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.servlet;

import static org.junit.Assert.*;

import java.io.IOException;

import static javax.servlet.AsyncContext.*;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.cruzeira.servlet.AsyncContext1;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;


public class AsyncContext1Test {

    @Test(expected = IllegalArgumentException.class)
    public void newWithNullValues() {
        new AsyncContext1(null, null);
    }

    @Test
    public void onCompleteListener() {
        AsyncContext asyncContext = new AsyncContext1(createRequest(), createResponse());
        TestAsyncListener listener = new TestAsyncListener();
        asyncContext.addListener(listener);

        asyncContext.complete();

        assertTrue(listener.onComplete);
    }

    @Test
    public void dispatch() {
        ServletRequest request = createRequest();
        AsyncContext asyncContext = new AsyncContext1(request, createResponse());
        asyncContext.dispatch();

        assertEquals("/test", request.getAttribute(ASYNC_REQUEST_URI));
        assertEquals("contextPath", request.getAttribute(ASYNC_CONTEXT_PATH));
        assertEquals("servletPath", request.getAttribute(ASYNC_SERVLET_PATH));
        assertEquals("name=test", request.getAttribute(ASYNC_QUERY_STRING));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void start() {
        Runnable run = new Runnable() {
            @Override
            public void run() {
            }
        };
        new AsyncContext1(createRequest(), createResponse()).start(run);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void addListener() {
        new AsyncContext1(createRequest(), createResponse()).addListener(new TestAsyncListener(), null, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void createListener() throws ServletException {
        new AsyncContext1(createRequest(), createResponse()).createListener(TestAsyncListener.class);
    }

    private ServletRequest createRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/test");
        request.setContextPath("contextPath");
        request.setServletPath("servletPath");
        request.setQueryString("name=test");
        return request;
    }

    private ServletResponse createResponse() {
        return new MockHttpServletResponse();
    }

    class TestAsyncListener implements AsyncListener {
        public boolean onComplete = false;

        @Override
        public void onComplete(AsyncEvent event) throws IOException {
            onComplete = true;
        }

        @Override
        public void onTimeout(AsyncEvent event) throws IOException {
        }

        @Override
        public void onError(AsyncEvent event) throws IOException {
        }

        @Override
        public void onStartAsync(AsyncEvent event) throws IOException {
        }

    }
}
