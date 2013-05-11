/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.tests;

import static org.junit.Assert.fail;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.cruzeira.server.Bootstrap;

/**
 * Base test class for tests that starts a basic cruzeira application and then
 * hit it with HTTP requests.
 * 
 */
public class AbstractFunctionalTest {

	static HttpClient client;
	final String url = "http://localhost:8080";

	public static void startServer() {
		// start the applicatoin
		Bootstrap.main(new String[] { "-dev" });
		client = new DefaultHttpClient();
	}

	public static void shutdownServer() {
		Bootstrap.shutdown();
	}

	/**
	 * Execute get or fail!
	 */
	public String getOrFail(String mapping) {
		try {
			return execAndReturnString(new HttpGet(url + mapping));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		} 
		return null;
	}

	public HttpResponse getResponseOrFail(String mapping) {
		HttpGet get = new HttpGet(url + mapping);
		try {
			return client.execute(get);
		} catch (Exception e) {
			fail(e.getMessage());
		}
		return null;
	}

	/**
	 * Execute a get or fail! The last parameter, header, must be multiple of 2
	 * because it is 'name, value, name, value, etc'.
	 */
	public HttpResponse getResponseOrFail(String mapping, String...header) {
		HttpGet get = new HttpGet(url + mapping);
		for (int i = 0; i < header.length; i += 2) {
			get.setHeader(header[i], header[i + 1]);
		}
		try {
			return client.execute(get);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		return null;
	}

	public String execAndReturnString(HttpUriRequest request) throws Exception {
		ResponseHandler<String> handler = new BasicResponseHandler();
		return client.execute(request, handler);
	}
	
	public void consume(HttpResponse response) {
		try {
			response.getEntity().getContent().close();
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public String get(String mapping) throws Exception {
		return execAndReturnString(new HttpGet(url + mapping));
	}

	public String delete(String mapping) throws Exception {
		return execAndReturnString(new HttpDelete(url + mapping));
	}

	public String post(String mapping) throws Exception {
		return execAndReturnString(new HttpPost(url + mapping));
	}

	public String put(String mapping) throws Exception {
		return execAndReturnString(new HttpPut(url + mapping));
	}

	public String options(String mapping) throws Exception {
		return execAndReturnString(new HttpOptions(url + mapping));
	}

	public String patch(String mapping) throws Exception {
		return execAndReturnString(new HttpPatch(url + mapping));
	}

	public String head(String mapping) throws Exception {
		return execAndReturnString(new HttpHead(url + mapping));
	}

	public String trace(String mapping) throws Exception {
		return execAndReturnString(new HttpTrace(url + mapping));
	}

}
