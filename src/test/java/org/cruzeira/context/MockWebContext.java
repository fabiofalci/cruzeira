/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.context;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;


public class MockWebContext implements WebContext {

    @Override
    public HttpServlet getHttpServlet() {
        return null;
    }

    @Override
    public void shutdown() {
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

}
