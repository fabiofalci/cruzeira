/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.servlet;

import io.netty.handler.codec.http.QueryStringDecoder;
import org.cruzeira.WebContext;
import org.springframework.util.Assert;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;


public class ServletRequest1 implements HttpServletRequest {

    final private String requestURI;
    final private String queryString;
    final private Map<String, String[]> parameters;
    final private WebContext webContext;
    final private String method;
    final private byte[] contentArray;
    final private String contentType;
    final private HttpSession httpSession;

    private Map<String, Object> attributes;
    private ServletInputStream1 servletInputStream;
    private BufferedReader reader;
    private Map<String, List<Object>> header;
    private AsyncContext asyncContext;
    private String characterEncoding = "utf-8";

    public ServletRequest1(String uri, WebContext webContext, String method, byte[] contentArray, String contentType, HttpSession httpSession) {
        Assert.notNull(uri, "Request URI cannot be null");
        Assert.notNull(webContext, "WebContext cannot be null");
        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        this.requestURI = decoder.path();
        this.queryString = uri.substring(this.requestURI.length());

        Map<String, List<String>> params = decoder.parameters();
        parameters = new HashMap<>(params.size());
        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            List<String> list = entry.getValue();
            parameters.put(entry.getKey(), list.toArray(new String[list.size()]));
        }

        this.webContext = webContext;
        this.method = method;
        this.contentType = contentType;

        if (contentArray != null) {
            this.contentArray = contentArray;
            if (this.contentType != null) {
                String[] split = contentType.split(";");
                String form = "application/x-www-form-urlencoded";
                boolean isForm = false;
                for (String s : split) {
                    if (form.equals(s)) {
                        isForm = true;
                        break;
                    }
                }
                if (isForm) {
                    String content = new String(contentArray);
                    decoder = new QueryStringDecoder(content, false);
                    params = decoder.parameters();
                    for (Map.Entry<String, List<String>> entry : params.entrySet()) {
                        List<String> list = entry.getValue();
                        parameters.put(entry.getKey(), list.toArray(new String[list.size()]));
                    }
                }
            }
        } else {
            this.contentArray = null;
        }

        this.httpSession = httpSession;
    }

    @Override
    public Object getAttribute(String name) {
        return getAttributes().get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return new Vector<>(getAttributes().keySet()).elements();
    }

    private Map<String, Object> getAttributes() {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        return attributes;
    }

    @Override
    public String getCharacterEncoding() {
        return this.characterEncoding;
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        this.characterEncoding = env;
    }

    @Override
    public int getContentLength() {
        if (contentArray != null) {
            return contentArray.length;
        }
        return -1;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (contentArray != null && reader == null && servletInputStream == null) {
            servletInputStream = new ServletInputStream1(contentArray);
        }
        return servletInputStream;
    }

    @Override
    public String getParameter(String name) {
        String[] value = parameters.get(name);
        if (value != null && value.length > 0) {
            return value[0];
        }
        return null;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return new Vector<>(parameters.keySet()).elements();
    }

    @Override
    public String[] getParameterValues(String name) {
        return parameters.get(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return parameters;
    }

    @Override
    public String getProtocol() {
        return "HTTP/1.1";
    }

    @Override
    public String getScheme() {
        return "http";
    }

    @Override
    public String getServerName() {
        return "getServerName";
    }

    @Override
    public int getServerPort() {
        return 8080;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (contentArray != null && servletInputStream == null && reader == null) {
            reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(contentArray)));
        }
        return reader;
    }

    @Override
    public String getRemoteAddr() {
        return "getRemoteAddr";
    }

    @Override
    public String getRemoteHost() {
        return "getRemoteHost";
    }

    @Override
    public void setAttribute(String name, Object o) {
        getAttributes().put(name, o);
    }

    @Override
    public void removeAttribute(String name) {
        // TODO Auto-generated method stub

    }

    @Override
    public Locale getLocale() {
        return Locale.US;
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return new Vector<>(Arrays.asList(getLocale())).elements();
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    public String getRealPath(String path) {
        return "getRealPath " + path;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return "getLocalName";
    }

    @Override
    public String getLocalAddr() {
        return "getLocalAddr";
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return this.webContext.getServletContext();
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        if (asyncContext == null) {
            asyncContext = new AsyncContext1(servletRequest, servletResponse);
        }
        return asyncContext;
    }

    @Override
    public boolean isAsyncStarted() {
        if (asyncContext == null) {
            return false;
        }
        // first time it will not have ASYNC_REQUEST_URI attribute
        return getAttribute(AsyncContext.ASYNC_REQUEST_URI) == null;
    }

    public boolean isAsync() {
        return asyncContext != null;
    }

    @Override
    public boolean isAsyncSupported() {
        return true;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return asyncContext;
    }

    @Override
    public DispatcherType getDispatcherType() {
        if (asyncContext != null) {
            return DispatcherType.ASYNC;
        }
        return null;
    }

    @Override
    public String getAuthType() {
        return "getAuthType";
    }

    @Override
    public Cookie[] getCookies() {
        return null;
    }

    @Override
    public long getDateHeader(String name) {
        List<Object> headers = getHeader().get(name);
        if (headers != null) {
            return (long) headers.get(0);
        }
        return -1;
    }

    @Override
    public String getHeader(String name) {
        Enumeration<String> headers = getHeaders(name);
        if (headers != null && headers.hasMoreElements()) {
            return headers.nextElement();
        }
        return null;
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        List<String> list = new ArrayList<>();
        List<Object> headers = getHeader().get(name);
        if (headers != null) {
            for (Object obj : headers) {
                list.add(obj.toString());
            }
        }
        return new Vector<>(list).elements();
    }

    private Map<String, List<Object>> getHeader() {
        if (header == null) {
            header = new HashMap<>();
        }
        return header;
    }

    public void addHeader(String name, Object value) {
        List<Object> values = getHeader().get(name);
        if (values == null) {
            values = new ArrayList<>(1);
            getHeader().put(name, values);
        }
        values.add(value);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return new Vector<>(getHeader().keySet()).elements();
    }

    @Override
    public int getIntHeader(String name) {
        List<Object> headers = getHeader().get(name);
        if (headers != null) {
            return (int) headers.get(0);
        }
        return -1;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getPathInfo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPathTranslated() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getContextPath() {
        return "";
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    @Override
    public String getRemoteUser() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isUserInRole(String role) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRequestURI() {
        return requestURI;
    }

    @Override
    public StringBuffer getRequestURL() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getServletPath() {
        return "";
    }

    @Override
    public HttpSession getSession(boolean create) {
        if (create) {
            return getSession();
        }
        return httpSession;
    }

    @Override
    public HttpSession getSession() {
        return httpSession;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void login(String username, String password) throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void logout() throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }

}
