/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.servlet;

import org.cruzeira.context.MockWebContext;
import org.junit.Test;

import javax.servlet.ServletContext;
import java.util.Enumeration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


public class ServletContext1Test {

    @Test
    public void contextPath() {
        assertEquals("", new ServletContext1(new MockWebContext()).getContextPath());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void contextPathWithUri() {
        new ServletContext1(new MockWebContext()).getContext("/someuri/test");
    }

    @Test
    public void versions() {
        ServletContext context = new ServletContext1(new MockWebContext());
        assertEquals(3, context.getMajorVersion());
        assertEquals(0, context.getMinorVersion());
        assertEquals(3, context.getEffectiveMajorVersion());
        assertEquals(0, context.getEffectiveMinorVersion());
    }

    @Test
    public void emptyInitParameters() {
        ServletContext context = new ServletContext1(new MockWebContext());
        Enumeration<String> initParameterNames = context.getInitParameterNames();
        assertFalse(initParameterNames.hasMoreElements());
    }

    @Test
    public void emptyAttributes() {
        ServletContext context = new ServletContext1(new MockWebContext());
        Enumeration<String> attributeNames = context.getAttributeNames();
        assertFalse(attributeNames.hasMoreElements());
    }

    @Test
    public void getSetInitParameters() {
        ServletContext context = new ServletContext1(new MockWebContext());
        // name0 = value0
        context.setInitParameter("name0", "value0");
        Enumeration<String> initParametersNames = context.getInitParameterNames();
        assertTrue(initParametersNames.hasMoreElements());
        assertEquals("name0", initParametersNames.nextElement());
        assertFalse(initParametersNames.hasMoreElements());

        assertEquals("value0", context.getInitParameter("name0"));
        assertNull(context.getInitParameter("name1"));

        // name0 = value0, name1 = value1
        context.setInitParameter("name1", "value1");
        initParametersNames = context.getInitParameterNames();
        assertTrue(initParametersNames.hasMoreElements());
        String name = initParametersNames.nextElement();
        assertTrue(name.equals("name0") || name.equals("name1"));
        assertTrue(initParametersNames.hasMoreElements());
        name = initParametersNames.nextElement();
        assertTrue(name.equals("name0") || name.equals("name1"));
        assertFalse(initParametersNames.hasMoreElements());

        assertEquals("value0", context.getInitParameter("name0"));
        assertEquals("value1", context.getInitParameter("name1"));

        // name0 = edited, name1 = value1
        assertFalse(context.setInitParameter("name0", "edited"));
        initParametersNames = context.getInitParameterNames();
        assertTrue(initParametersNames.hasMoreElements());
        name = initParametersNames.nextElement();
        assertTrue(name.equals("name0") || name.equals("name1"));
        assertTrue(initParametersNames.hasMoreElements());
        name = initParametersNames.nextElement();
        assertTrue(name.equals("name0") || name.equals("name1"));
        assertFalse(initParametersNames.hasMoreElements());

        assertEquals("value0", context.getInitParameter("name0"));
        assertEquals("value1", context.getInitParameter("name1"));
    }

    @Test
    public void getSetAttributes() {
        ServletContext context = new ServletContext1(new MockWebContext());

        // name0 = value0
        context.setAttribute("name0", "value0");
        Enumeration<String> attributeNames = context.getAttributeNames();
        assertTrue(attributeNames.hasMoreElements());
        assertEquals("name0", attributeNames.nextElement());
        assertFalse(attributeNames.hasMoreElements());

        assertEquals("value0", context.getAttribute("name0"));
        assertNull(context.getAttribute("name1"));

        // name0 = value0, name1 = value1
        context.setAttribute("name1", "value1");
        attributeNames = context.getAttributeNames();
        assertTrue(attributeNames.hasMoreElements());
        String name = attributeNames.nextElement();
        assertTrue(name.equals("name0") || name.equals("name1"));
        assertTrue(attributeNames.hasMoreElements());
        name = attributeNames.nextElement();
        assertTrue(name.equals("name0") || name.equals("name1"));
        assertFalse(attributeNames.hasMoreElements());

        assertEquals("value0", context.getAttribute("name0"));
        assertEquals("value1", context.getAttribute("name1"));

        // name0 = edited, name1 = value1
        context.setAttribute("name0", "edited");
        attributeNames = context.getAttributeNames();
        assertTrue(attributeNames.hasMoreElements());
        name = attributeNames.nextElement();
        assertTrue(name.equals("name0") || name.equals("name1"));
        assertTrue(attributeNames.hasMoreElements());
        name = attributeNames.nextElement();
        assertTrue(name.equals("name0") || name.equals("name1"));
        assertFalse(attributeNames.hasMoreElements());

        assertEquals("edited", context.getAttribute("name0"));
        assertEquals("value1", context.getAttribute("name1"));

        // name0 = edited
        context.removeAttribute("name1");
        attributeNames = context.getAttributeNames();
        assertTrue(attributeNames.hasMoreElements());
        assertEquals("name0", attributeNames.nextElement());
        assertFalse(attributeNames.hasMoreElements());

        assertEquals("edited", context.getAttribute("name0"));
        assertNull(context.getAttribute("name1"));

        // empty
        context.setAttribute("name0", null);
        attributeNames = context.getAttributeNames();
        assertFalse(attributeNames.hasMoreElements());

        assertNull(context.getAttribute("name0"));
        assertNull(context.getAttribute("name1"));
    }

    // well, ServletContext1 has a lot of UnsupportedOperationExceptions. Fix it
    // first!

}
