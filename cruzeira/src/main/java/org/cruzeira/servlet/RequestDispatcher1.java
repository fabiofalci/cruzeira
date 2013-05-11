/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.jasper.runtime.HttpJspBase;
import org.cruzeira.context.WebContext;


/**
 * Implementation of {@link RequestDispatcher} that forward/include jsp to its
 * class file.
 * 
 * <p>
 * FIXME revise forward/include contracts. For now they're doing exactly the
 * same thing.
 * 
 */
public class RequestDispatcher1 implements RequestDispatcher {
	private String path;
	private WebContext webContext;

	public RequestDispatcher1(String path, WebContext webContext) {
		if (path.startsWith("/")) {
			path = path.substring(1, path.length());
		}
		this.path = getClassName(path);
		this.webContext = webContext;
	}

	@Override
	public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
		try {
			Class<?> jspClass = Class.forName(path);
			HttpJspBase jspObject = (HttpJspBase) jspClass.newInstance();
			jspObject.init(webContext.getServletConfig());
			jspObject.service(request, response);
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	@Override
	public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
		try {
			Class<?> jspClass = Class.forName(path);
			HttpJspBase jspObject = (HttpJspBase) jspClass.newInstance();
			jspObject.init(webContext.getServletConfig());
			jspObject.service(request, response);
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	/**
	 * Returns the class name of the jsp file. This method has issues when using
	 * relative navigation, like ../../file.jsp. For now, the prefered way is to
	 * use absolute path.
	 */
	private String getClassName(String path) {
		if (path.startsWith("..")) {
			path = path.replace("..", "");
		}
		if (path.endsWith(".jsp")) {
			path = path.replace(".jsp", "_jsp");
		} else {
			path = path + "_jsp";
		}
		path = path.replace("/", ".");
		if (path.startsWith("views")) {
			return path;
		}

		if (!path.startsWith(".")) {
			path = "." + path;
		}
		return "views" + path;
	}

}