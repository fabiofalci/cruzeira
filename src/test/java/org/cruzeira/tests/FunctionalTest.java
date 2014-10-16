/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.tests;

import io.netty.handler.codec.http.HttpHeaders;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FunctionalTest extends AbstractFunctionalTest {

    @BeforeClass
    public static void beforeClass() {
        startServer();
    }

    @AfterClass
    public static void afterClass() {
        shutdownServer();
    }

    @Test
    public void simple() {
        String response = getOrFail("/simple");
        assertEquals("simple", response);
    }

    @Test
    public void asyncSimple() {
        String response = getOrFail("/asyncSimple");
        assertEquals("asyncSimple", response);
    }

    @Test
    public void printWriter() {
        String str = getOrFail("/printWriter");
        assertEquals("Using print writer response", str);
    }

    @Test
    public void cookie() {
        String requestCookie = "app-name=cruzeira";
        HttpResponse response = getResponseOrFail("/simple", HttpHeaders.Names.COOKIE, requestCookie);
        consume(response);
        String responseCookie = response.getFirstHeader(HttpHeaders.Names.SET_COOKIE).getValue();
        assertEquals(requestCookie, responseCookie);

        // because if you keep the connection it will include a cookie exactly
        // how it received from server
        client = HttpClientBuilder.create().build();

        String requestCookie2 = "os=skynet";
        response = getResponseOrFail("/simple", HttpHeaders.Names.COOKIE, requestCookie + "; "
                + requestCookie2);
        consume(response);
        Header[] headers = response.getHeaders(HttpHeaders.Names.SET_COOKIE);
        assertEquals(2, headers.length);

        assertTrue(requestCookie.equals(headers[0].getValue())
                || requestCookie2.equals(headers[0].getValue()));

        assertTrue(requestCookie.equals(headers[1].getValue())
                || requestCookie2.equals(headers[1].getValue()));
    }

}
