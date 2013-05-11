/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.netty;

import static org.junit.Assert.assertEquals;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.cruzeira.tests.AbstractFunctionalTest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.junit.AfterClass;
import org.junit.Test;

public class ResourcesChannelHandlerTest extends AbstractFunctionalTest {

	static {
		startServer();
	}

	@AfterClass
	public static void shutdown() {
		shutdownServer();
	}

	@Test
	public void favicon() throws Exception {
		try {
			get("/favicon.ico");
		} catch (HttpResponseException e) {
			assertEquals(HttpResponseStatus.NOT_FOUND.getCode(), e.getStatusCode());
		}
	}

	@Test
	public void notFound() throws Exception {
		try {
			get("/resources/notFound.txt");
		} catch (HttpResponseException e) {
			assertEquals(HttpResponseStatus.NOT_FOUND.getCode(), e.getStatusCode());
		}
	}

	@Test
	public void directory() throws Exception {
		try {
			get("/resources/dir");
		} catch (HttpResponseException e) {
			assertEquals(HttpResponseStatus.FORBIDDEN.getCode(), e.getStatusCode());
		}
	}

	@Test
	public void webjar() throws Exception {
		try {
			get("/webjars/some");
		} catch (HttpResponseException e) {
			assertEquals(HttpResponseStatus.NOT_FOUND.getCode(), e.getStatusCode());
		}
		// FIXME create a successful webjar test
	}

	@Test
	public void resource() {
		String content = getOrFail("/resources/resource.txt");
		assertEquals("resource content", content);
	}

	@Test
	public void resourceNotModified() {
		HttpResponse response = getResponseOrFail("/resources/resource.txt");
		String date = response.getFirstHeader(HttpHeaders.Names.LAST_MODIFIED).getValue();
		consume(response);
		response = getResponseOrFail("/resources/resource.txt", HttpHeaders.Names.IF_MODIFIED_SINCE, date);
		assertEquals(HttpResponseStatus.NOT_MODIFIED.getCode(), response.getStatusLine().getStatusCode());
	}
	
	@Test
	public void security() throws Exception {
		try {
			get("/resources/dir/../../somefile.txt");
		} catch (HttpResponseException e) {
			assertEquals(HttpResponseStatus.FORBIDDEN.getCode(), e.getStatusCode());
		}
	}

	@Test
	public void security2() throws Exception {
		try {
			get("./resources/resource.txt");
		} catch (HttpResponseException e) {
			assertEquals(HttpResponseStatus.FORBIDDEN.getCode(), e.getStatusCode());
		}
	}

	@Test
	public void security3() throws Exception {
		try {
			get("/resources/resource.txt/..");
		} catch (HttpResponseException e) {
			assertEquals(HttpResponseStatus.FORBIDDEN.getCode(), e.getStatusCode());
		}
	}

	@Test
	public void security4() throws Exception {
		try {
			get("/resources/dir./file.txt");
		} catch (HttpResponseException e) {
			assertEquals(HttpResponseStatus.FORBIDDEN.getCode(), e.getStatusCode());
		}
	}

	@Test
	public void security5() throws Exception {
		try {
			get("/resources/dir/.file.txt");
		} catch (HttpResponseException e) {
			assertEquals(HttpResponseStatus.FORBIDDEN.getCode(), e.getStatusCode());
		}
	}

	// only get is supported!

	@Test
	public void delete() throws Exception {
		try {
			delete("/resources/resource.txt");
		} catch (HttpResponseException e) {
			assertEquals(HttpResponseStatus.METHOD_NOT_ALLOWED.getCode(), e.getStatusCode());
		}
	}

	@Test
	public void post() throws Exception {
		try {
			post("/resources/resource.txt");
		} catch (HttpResponseException e) {
			assertEquals(HttpResponseStatus.METHOD_NOT_ALLOWED.getCode(), e.getStatusCode());
		}
	}

	@Test
	public void put() throws Exception {
		try {
			put("/resources/resource.txt");
		} catch (HttpResponseException e) {
			assertEquals(HttpResponseStatus.METHOD_NOT_ALLOWED.getCode(), e.getStatusCode());
		}
	}

	@Test
	public void options() throws Exception {
		try {
			options("/resources/resource.txt");
		} catch (HttpResponseException e) {
			assertEquals(HttpResponseStatus.METHOD_NOT_ALLOWED.getCode(), e.getStatusCode());
		}
	}

	@Test
	public void patch() throws Exception {
		try {
			patch("/resources/resource.txt");
		} catch (HttpResponseException e) {
			assertEquals(HttpResponseStatus.METHOD_NOT_ALLOWED.getCode(), e.getStatusCode());
		}
	}

	@Test
	public void head() throws Exception {
		try {
			head("/resources/resource.txt");
		} catch (HttpResponseException e) {
			assertEquals(HttpResponseStatus.METHOD_NOT_ALLOWED.getCode(), e.getStatusCode());
		}
	}

	@Test
	public void trace() throws Exception {
		try {
			trace("/resources/resource.txt");
		} catch (HttpResponseException e) {
			assertEquals(HttpResponseStatus.METHOD_NOT_ALLOWED.getCode(), e.getStatusCode());
		}
	}

}
