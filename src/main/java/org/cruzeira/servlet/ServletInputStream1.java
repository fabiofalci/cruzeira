/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.servlet;

import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Basic implementantion of {@link ServletInputStream} that uses a
 * {@link ByteBuffer}
 */
public class ServletInputStream1 extends ServletInputStream {

    private ByteBuffer buffer;

    public ServletInputStream1(byte[] content) {
        buffer = ByteBuffer.allocate(content.length);
        buffer.put(content);
        buffer.rewind();
    }

    @Override
    public int read() throws IOException {
        try {
            return buffer.get();
        } catch (BufferUnderflowException be) {
            return -1;
        }
    }

}
