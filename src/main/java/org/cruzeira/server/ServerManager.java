/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.server;

import org.cruzeira.context.SpringContext;
import org.cruzeira.context.WebContext;

import javax.servlet.http.HttpServlet;

/**
 * This manager provides access to spring context and its dispatcher
 * servlet
 */
public class ServerManager {

    private HttpServlet httpServlet;
    private WebContext webContext;

    public ServerManager() {
        try {
            webContext = new SpringContext();
            httpServlet = webContext.getHttpServlet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HttpServlet getDispatcherServlet() {
        return httpServlet;
    }

    public WebContext getSpringContext() {
        return webContext;
    }

}
