/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.servlet;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;


public class ServletOutputStream1Test {

    @Test
    public void write() throws IOException {
        try (ServletOutputStream1 output = new ServletOutputStream1()) {
            output.write("1".getBytes()[0]);
            assertEquals("1", output.getStringWriter().getBuffer().toString());
        }
    }

}
