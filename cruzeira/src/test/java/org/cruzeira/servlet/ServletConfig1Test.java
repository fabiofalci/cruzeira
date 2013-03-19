/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.servlet;

import static org.junit.Assert.*;

import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.cruzeira.servlet.ServletConfig1;
import org.junit.Test;
import org.springframework.mock.web.MockServletContext;


public class ServletConfig1Test {

	@Test
	public void emptyParameters() {
		ServletConfig config = new ServletConfig1(new MockServletContext());
		Enumeration<String> initParameterNames = config.getInitParameterNames();
		assertFalse(initParameterNames.hasMoreElements());
	}

	@Test
	public void parameters() {
		ServletContext context = new MockServletContext();
		context.setInitParameter("name0", "value0");
		ServletConfig config = new ServletConfig1(context);
		
		Enumeration<String> initParameterNames = config.getInitParameterNames();
		assertTrue(initParameterNames.hasMoreElements());
		initParameterNames.nextElement();
		assertFalse(initParameterNames.hasMoreElements());

		context.setInitParameter("name1", "value1");

		initParameterNames = config.getInitParameterNames();
		assertTrue(initParameterNames.hasMoreElements());
		initParameterNames.nextElement();
		assertTrue(initParameterNames.hasMoreElements());
		initParameterNames.nextElement();
		assertFalse(initParameterNames.hasMoreElements());
	}

}
