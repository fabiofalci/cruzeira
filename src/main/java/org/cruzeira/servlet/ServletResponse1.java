/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class ServletResponse1 implements HttpServletResponse {

    private int status = SC_OK;
    private Map<String, List<Object>> header;
    private String contentType = "text/html;charset=UTF-8<";
    private boolean commited = false;
    private ServletOutputStream1 servletOutputStream;
    private String charset;
    private int bufferSize;
    private Integer errorStatusCode;
    private String errorMessage;
    private StringWriter stringWriter;
    private PrintWriter writer;

    @Override
    public String getCharacterEncoding() {
        return charset;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (servletOutputStream == null) {
            servletOutputStream = new ServletOutputStream1();
            stringWriter = servletOutputStream.getStringWriter();
        }
        return servletOutputStream;
    }

    public void closeStreams() {
        if (servletOutputStream != null) {
            try {
                servletOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (writer != null) {
            writer.close();
        }
    }


    public StringWriter getStringWriter() {
        return stringWriter;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (stringWriter == null) {
            stringWriter = new StringWriter();
            writer = new PrintWriter(stringWriter);
        }
        return writer;
    }

    @Override
    public void setCharacterEncoding(String charset) {
        this.charset = charset;
    }

    @Override
    public void setContentLength(int len) {
        setIntHeader("Content-Length", len);
    }

    @Override
    public void setContentType(String type) {
        this.contentType = type;
    }

    @Override
    public void setBufferSize(int size) {
        this.bufferSize = size;
    }

    @Override
    public int getBufferSize() {
        return bufferSize;
    }

    @Override
    public void flushBuffer() throws IOException {
        closeStreams();
        commited = true;
    }

    @Override
    public void resetBuffer() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isCommitted() {
        return commited;
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub

    }

    @Override
    public void setLocale(Locale loc) {
        // TODO Auto-generated method stub

    }

    @Override
    public Locale getLocale() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addCookie(Cookie cookie) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean containsHeader(String name) {
        return header.containsKey(name);
    }

    @Override
    public String encodeURL(String url) {
        return url;
    }

    @Override
    public String encodeRedirectURL(String url) {
        return url;
    }

    @Override
    public String encodeUrl(String url) {
        return url;
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return url;
    }

    public boolean isError() {
        return errorStatusCode != null;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        this.errorStatusCode = sc;
        this.errorMessage = msg;
        setStatus(this.errorStatusCode);
    }

    @Override
    public void sendError(int sc) throws IOException {
        sendError(sc, null);
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        System.out.println("Send redirect " + location);
        setStatus(SC_FOUND);
        setHeader("Location", location);
    }

    @Override
    public void setDateHeader(String name, long date) {
        List<Object> col = new ArrayList<>();
        col.add(date);
        getHeader().put(name, col);
    }

    @Override
    public void addDateHeader(String name, long date) {
        Collection col = getHeadersInternal(name);
        if (col == null) {
            List<Object> list = new ArrayList<>();
            getHeader().put(name, list);
            col = list;
        }
        col.add(date);
    }

    @Override
    public void setHeader(String name, String value) {
        List<Object> col = new ArrayList<>();
        col.add(value);
        getHeader().put(name, col);
    }

    @Override
    public void addHeader(String name, String value) {
        Collection col = getHeadersInternal(name);
        if (col == null) {
            List<Object> list = new ArrayList<>();
            getHeader().put(name, list);
            col = list;
        }
        col.add(value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        List<Object> col = new ArrayList<>();
        col.add(value);
        getHeader().put(name, col);
    }

    @Override
    public void addIntHeader(String name, int value) {
        Collection col = getHeadersInternal(name);
        if (col == null) {
            List<Object> list = new ArrayList<>();
            getHeader().put(name, list);
            col = list;
        }
        col.add(value);
    }

    @Override
    public void setStatus(int sc) {
        setStatus(sc, null);
    }

    @Override
    public void setStatus(int sc, String sm) {
        this.status = sc;
        // TODO sm
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public String getHeader(String name) {
        Collection<String> headers = getHeaders(name);
        if (headers != null && headers.iterator().hasNext()) {
            return headers.iterator().next();
        }
        return null;
    }

    @Override
    public Collection<String> getHeaders(String name) {
        List<String> list = new ArrayList<>();
        Collection<Object> headers = getHeadersInternal(name);
        if (headers != null) {
            for (Object obj : headers) {
                list.add(obj.toString());
            }
        }
        return list;
    }

    public Collection<Object> getHeadersInternal(String name) {
        return getHeader().get(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return new ArrayList<>(getHeader().keySet());
    }

    private Map<String, List<Object>> getHeader() {
        if (header == null) {
            header = new HashMap<>();
        }
        return header;
    }
}
