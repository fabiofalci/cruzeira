/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.server;

import java.net.URLClassLoader;

import static org.junit.Assert.*;
import org.cruzeira.context.MockWebContext;
import org.cruzeira.filesystem.FileSystemChangesAlwaysTrue;
import org.junit.Test;

public class ServerManagerTest {

	@Test
	public void reload() {
		URLClassLoader originalClassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
		
		URLClassLoader newClassLoader = new URLClassLoader(originalClassLoader.getURLs(), null);
		Thread.currentThread().setContextClassLoader(newClassLoader);
		
		ServerManager serverManager = new ServerManager(new FileSystemChangesAlwaysTrue(), MockWebContext.class);
		ClassLoader firstClassLoader = serverManager.getClassLoader();

		serverManager.beforeRequest();

		Thread.currentThread().setContextClassLoader(originalClassLoader);
		assertNotEquals(firstClassLoader, serverManager.getClassLoader());
	}
}
