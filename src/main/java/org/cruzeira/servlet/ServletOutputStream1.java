/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.servlet;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.StringWriter;

/**
 * A basic implementation of {@link ServletOutputStream} that uses a
 * {@link StringWriter}
 *
 * @author frodrigues
 */
public class ServletOutputStream1 extends ServletOutputStream {

    private StringWriter stringWriter;

    public ServletOutputStream1() {
        this.stringWriter = new StringWriter();
    }

    @Override
    public void write(int b) throws IOException {
        stringWriter.write(b);
    }

    public StringWriter getStringWriter() {
        return stringWriter;
    }
}
