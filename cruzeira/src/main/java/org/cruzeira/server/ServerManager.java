/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;

import org.cruzeira.context.SpringContext;
import org.cruzeira.context.WebContext;
import org.cruzeira.filesystem.FileSystemChanges;
import org.cruzeira.filesystem.FileSystemChangesNIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This manager controls when cruzeira needs to recompile and reload the project
 * classes. Additionally it provides access to spring context and its dispatcher
 * servlet. Maybe it should be divided in more classes.
 * 
 */
public class ServerManager {

	private Object httpServlet;
	private Object webContext;
	private URLClassLoader classLoader;
	private FileSystemChanges projectChanges;
	private final Class<? extends WebContext> webContextClass;
	
	final Logger logger = LoggerFactory.getLogger(ServerManager.class);

	public ServerManager() {
		this(new FileSystemChangesNIO("src"), null);
	}
	
	public ServerManager(FileSystemChanges fileSystemChanges, Class<? extends WebContext> webContextClass) {
		if (webContextClass == null) {
			this.webContextClass = SpringContext.class;
		} else {
			this.webContextClass = webContextClass;
		}
		this.projectChanges = fileSystemChanges;
		URLClassLoader urlClassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
		init(urlClassLoader);
	}

	private void init(ClassLoader classLoader) {
		try {
			if (this.classLoader instanceof URLClassLoader) {
				this.classLoader.close();
			}
			this.classLoader = (URLClassLoader) classLoader;

			Thread.currentThread().setContextClassLoader(classLoader);

			Class<?> webContextClassNew = classLoader.loadClass(webContextClass.getName());
			webContext = webContextClassNew.newInstance();

			httpServlet = webContext.getClass().getMethod("getHttpServlet").invoke(webContext);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.projectChanges.reload();
	}

	/**
	 * JDBC drivers keeps some memory references in DriverManager. Because of
	 * these referentes, the PermGen memory overflows in a few reloads. You can
	 * easily see the overflow using visualvm. The solution removes all these
	 * references clearing a private list in DriverManager. 
	 */
	private void removeDrivers() {
		// very promising but doesn't remove from memory
		// try {
		// Enumeration<Driver> drivers = DriverManager.getDrivers();
		// while (drivers.hasMoreElements()) {
		// DriverManager.deregisterDriver(drivers.nextElement());
		// }
		// } catch (Exception e1) {
		// e1.printStackTrace();
		// }

		if (classLoader != null) {
			try {
				Class<?> clazz = getClassLoader().loadClass("java.sql.DriverManager");
				Field field = clazz.getDeclaredField("registeredDrivers");
				field.setAccessible(true);
				Object obj = field.get(null);
				obj.getClass().getMethod("clear").invoke(obj);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void shutdown() {
		if (webContext != null) {
			try {
				webContext.getClass().getMethod("shutdown").invoke(webContext);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		removeDrivers();
	}

	public void beforeRequest() {
		if (hasChanges()) {
			shutdown();
			recompile();
			URLClassLoader urlClassLoader = (URLClassLoader) classLoader;

			init(createClassLoader(urlClassLoader.getURLs()));
		}
	}

	private ClassLoader createClassLoader(URL[] urls) {
		return new URLClassLoader(urls, null);
	}

	private void recompile() {
		try {
			Process process = Runtime.getRuntime().exec("mvn compile");
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				logger.debug("Compiling: {}", line);
			}
			process.waitFor();
			logger.debug("Recompile exit status {}", process.exitValue());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean hasChanges() {
		return projectChanges.hasChanges();
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public Object getDispatcherServlet() {
		return httpServlet;
	}

	public Object getSpringContext() {
		return webContext;
	}

}
