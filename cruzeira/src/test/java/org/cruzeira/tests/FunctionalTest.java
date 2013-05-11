/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.junit.AfterClass;
import org.junit.Test;

public class FunctionalTest extends AbstractFunctionalTest {

	static {
		startServer();
	}

	@AfterClass
	public static void shutdown() {
		shutdownServer();
	}

	@Test
	public void simple() {
		String response = getOrFail("/simple");
		assertEquals("simple", response);

	}

	@Test
	public void async() {
		String response = getOrFail("/async");
		assertEquals("JSP file", response);
	}

	@Test
	public void resource() {
		String resource = getOrFail("/resources/resource.txt");
		assertEquals("resource content", resource);
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
		client = new DefaultHttpClient();

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

	@Test
	public void runtimeException() throws Exception {
		try {
			get("/runtimeException");
			fail();
		} catch (HttpResponseException e) {
			assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR.getCode(), e.getStatusCode());
		}
	}
	
	@Test
	public void exception() throws Exception {
		try {
			get("/exception");
			fail();
		} catch (HttpResponseException e) {
			assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR.getCode(), e.getStatusCode());
		}
	}
	
	@Test
	public void error() throws Exception {
		try {
			get("/error");
			fail();
		} catch (HttpResponseException e) {
			assertEquals(HttpResponseStatus.NOT_IMPLEMENTED.getCode(), e.getStatusCode());
		}		
	}
	
	@Test
	public void printWriter() {
		String str = getOrFail("/printWriter");
		assertEquals("Using print writer response", str);
	}
	
	@Test
	public void jsp() {
		String response = getOrFail("/jsp");
		assertEquals("JSP file", response);
	}
	
	@Test
	public void jspWithInclude() {
		String response = getOrFail("/jspWithInclude");
		assertEquals("1 JSP file 2 JSP file 3 JSP file 4 JSP file", response.trim());
	}
	
	@Test
	public void jspIncludesNotFound() throws Exception {
		try {
			get("/jspIncludesNotFound");
			fail();
		} catch (HttpResponseException e) {
			assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR.getCode(), e.getStatusCode());
		}		
	}
	
}
