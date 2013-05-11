/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.tests;

import static org.junit.Assert.assertEquals;

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
	public void jsp() {
		String response = getOrFail("/jsp");
		assertEquals("JSP file", response);
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

}
