/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.context;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;

/**
 * This web context is the entry point to the servlet world. Here the servlet
 * implementation must be initialized and must provide access to the various
 * servlet classes (like config and context).
 *
 * @see SpringContext
 */
public interface WebContext {

    /**
     * Provides access to the HttpServlet used by the servlet
     * implementation. It is this servlet that will be used by cruzeira to handle
     * all incoming request.
     */
    HttpServlet getHttpServlet();

    /**
     * Executes a graceful shutdown in the servlet world.
     */
    void shutdown();

    /**
     * Return the ServletContext used by the servlet
     */
    ServletContext getServletContext();

}
