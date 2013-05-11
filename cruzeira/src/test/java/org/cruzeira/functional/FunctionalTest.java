/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.functional;

import static org.junit.Assert.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.cruzeira.server.Bootstrap;
import org.junit.Test;

public class FunctionalTest {

	static HttpClient client;
	static {
		Bootstrap.main(null);
		client = new DefaultHttpClient();
	}

	public String get(String mapping) {
		HttpGet get = new HttpGet("http://localhost:8080" + mapping);
		ResponseHandler<String> handler = new BasicResponseHandler();
		try {
			return client.execute(get, handler);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		return null;
	}

	@Test
	public void simple() {
		String response = get("/simple");
		assertEquals("simple", response);
	}

	@Test
	public void jsp() {
		String response = get("/jsp");
		assertEquals("JSP file", response);
	}
	
	@Test
	public void async() {
		String response = get("/async");
		assertEquals("JSP file", response);
	}

}
