/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.netty;

import org.apache.http.client.HttpResponseException;
import org.cruzeira.tests.AbstractFunctionalTest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.junit.AfterClass;
import static org.junit.Assert.*;
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
		getOrFail("/resources/resource.txt");
		getOrFail("/resources/resource.txt");
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
