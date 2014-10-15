/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.context;

import javax.servlet.ServletConfig;
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
    public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

}
