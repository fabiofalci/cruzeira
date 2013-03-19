/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.servlet;

import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * Basic implementation of {@link ServletConfig}
 * 
 */
public class ServletConfig1 implements ServletConfig {

	private ServletContext servletContext;

	public ServletConfig1(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	@Override
	public String getServletName() {
		return "cruzeira";
	}

	@Override
	public String getInitParameter(String name) {
		return servletContext.getInitParameter(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return servletContext.getInitParameterNames();
	}

	@Override
	public ServletContext getServletContext() {
		return servletContext;
	}

}
