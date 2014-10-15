/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.context;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.cruzeira.servlet.ServletConfig1;
import org.cruzeira.servlet.ServletContext1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;


/**
 * Spring implementation of {@link WebContext}. It is based on
 * {@link XmlWebApplicationContext}.
 */
public class SpringContext implements WebContext {

    final Logger logger = LoggerFactory.getLogger(SpringContext.class);

    private WebApplicationContext webAppContext;
    private DispatcherServlet dispatcherServlet;
    private ServletConfig servletConfig;
    private ServletContext servletContext;

    public SpringContext() throws ServletException {
        this("classpath:app-context.xml");
    }

    public SpringContext(String configLocation) throws ServletException {
        logger.debug("Initializing spring context");
        XmlWebApplicationContext appContext = new XmlWebApplicationContext();
        webAppContext = appContext;
        ContextLoader contextLoader = new ContextLoader(webAppContext);
        servletContext = new ServletContext1(this);
        servletContext.setInitParameter(ContextLoader.CONFIG_LOCATION_PARAM, configLocation);
        contextLoader.initWebApplicationContext(servletContext);

        dispatcherServlet = new DispatcherServlet(webAppContext);

        servletConfig = new ServletConfig1(servletContext);
        dispatcherServlet.init(servletConfig);
    }

    public void shutdown() {
        logger.debug("Spring context shutdown in progress");
        AbstractApplicationContext appContext = (AbstractApplicationContext) webAppContext;
        appContext.destroy();
        dispatcherServlet.destroy();
    }

    public HttpServlet getHttpServlet() {
        return dispatcherServlet;
    }

    public DispatcherServlet getDispatcherServlet() {
        return dispatcherServlet;
    }

    public ServletConfig getServletConfig() {
        return servletConfig;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

}
