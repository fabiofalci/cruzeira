/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.servlet;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.servlet.ServletInputStream;

import org.cruzeira.servlet.ServletInputStream1;
import org.junit.Test;


public class ServletInputStream1Test {

    @Test
    public void readLine() throws IOException {
        byte[] original = "foo bar!".getBytes();
        try (ServletInputStream input = new ServletInputStream1(original)) {

            byte[] read = new byte[original.length];
            input.readLine(read, 0, read.length);

            assertEquals(original.length, read.length);

            for (int i = 0; i < read.length; i++) {
                assertEquals(original[i], read[i]);
            }
        }
    }

    @Test
    public void read() throws IOException {
        byte[] original = "foo bar!".getBytes();
        try (ServletInputStream input = new ServletInputStream1(original)) {

            for (int i = 0; i < original.length; i++) {
                assertEquals(original[i], input.read());
            }
        }
    }
}
