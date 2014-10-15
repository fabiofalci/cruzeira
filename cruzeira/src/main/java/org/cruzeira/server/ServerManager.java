/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.server;

import org.cruzeira.context.SpringContext;
import org.cruzeira.context.WebContext;

import javax.servlet.http.HttpServlet;

/**
 * This manager controls when cruzeira needs to recompile and reload the project
 * classes. Additionally it provides access to spring context and its dispatcher
 * servlet. Maybe it should be divided in more classes.
 */
public class ServerManager {

    private HttpServlet httpServlet;
    private WebContext webContext;

    public ServerManager() {
        this(null);
    }

    public ServerManager(Class<? extends WebContext> webContextClass) {
        if (webContextClass == null) {
            webContextClass = SpringContext.class;
        }
        try {
            Class<?> webContextClassNew = Class.forName(webContextClass.getName());
            webContext = (WebContext) webContextClassNew.newInstance();
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
