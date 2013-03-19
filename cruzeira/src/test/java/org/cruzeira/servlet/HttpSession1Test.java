/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.servlet;

import static org.junit.Assert.*;

import java.util.Enumeration;

import javax.servlet.http.HttpSession;

import org.cruzeira.servlet.HttpSession1;
import org.junit.Test;
import org.springframework.mock.web.MockServletContext;


public class HttpSession1Test {

	@Test
	public void attributesEmpty() {
		HttpSession session = new HttpSession1(new MockServletContext());
		assertFalse(session.getAttributeNames().hasMoreElements());
	}

	@Test
	public void setAndGetAttributes() {
		HttpSession session = new HttpSession1(new MockServletContext());

		// name0 = value0
		session.setAttribute("name0", "value0");
		Enumeration<String> attributeNames = session.getAttributeNames();
		assertTrue(attributeNames.hasMoreElements());
		assertEquals("name0", attributeNames.nextElement());
		assertFalse(attributeNames.hasMoreElements());

		assertEquals("value0", session.getAttribute("name0"));
		assertNull(session.getAttribute("name1"));

		// name0 = value0, name1 = value1
		session.setAttribute("name1", "value1");
		attributeNames = session.getAttributeNames();
		assertTrue(attributeNames.hasMoreElements());
		String name = attributeNames.nextElement();
		assertTrue(name.equals("name0") || name.equals("name1"));
		assertTrue(attributeNames.hasMoreElements());
		name = attributeNames.nextElement();
		assertTrue(name.equals("name0") || name.equals("name1"));
		assertFalse(attributeNames.hasMoreElements());

		assertEquals("value0", session.getAttribute("name0"));
		assertEquals("value1", session.getAttribute("name1"));

		// name0 = edited, name1 = value1
		session.setAttribute("name0", "edited");
		attributeNames = session.getAttributeNames();
		assertTrue(attributeNames.hasMoreElements());
		name = attributeNames.nextElement();
		assertTrue(name.equals("name0") || name.equals("name1"));
		assertTrue(attributeNames.hasMoreElements());
		name = attributeNames.nextElement();
		assertTrue(name.equals("name0") || name.equals("name1"));
		assertFalse(attributeNames.hasMoreElements());

		assertEquals("edited", session.getAttribute("name0"));
		assertEquals("value1", session.getAttribute("name1"));

		// name0 = edited
		session.removeAttribute("name1");
		attributeNames = session.getAttributeNames();
		assertTrue(attributeNames.hasMoreElements());
		assertEquals("name0", attributeNames.nextElement());
		assertFalse(attributeNames.hasMoreElements());

		assertEquals("edited", session.getAttribute("name0"));
		assertNull(session.getAttribute("name1"));

		// empty
		session.setAttribute("name0", null);
		attributeNames = session.getAttributeNames();
		assertFalse(attributeNames.hasMoreElements());

		assertNull(session.getAttribute("name0"));
		assertNull(session.getAttribute("name1"));
	}

}
