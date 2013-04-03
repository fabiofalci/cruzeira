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
import org.cruzeira.filesystem.FileSystemChangesNIO;


public class ServerManager {

	private Object dispatcherServlet;
	private Object springContext;
	private URLClassLoader classLoader;
	private FileSystemChangesNIO projectChanges;

	public ServerManager() {
		URLClassLoader urlClassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
		init(urlClassLoader);
	}

	public void init(ClassLoader classLoader) {
		try {
			if (this.classLoader instanceof URLClassLoader) {
				this.classLoader.close();
			}
			this.classLoader = (URLClassLoader) classLoader;

			Thread.currentThread().setContextClassLoader(classLoader);

			Class<?> springContextClass = classLoader.loadClass(SpringContext.class.getName());
			springContext = springContextClass.newInstance();

			dispatcherServlet = springContext.getClass().getMethod("getDispatcherServlet").invoke(springContext);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.projectChanges = new FileSystemChangesNIO("src");
	}

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
				System.out.println(obj);
				obj.getClass().getMethod("clear").invoke(obj);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void shutdown() {
		projectChanges.shutdown();
		if (springContext != null) {
			try {
				springContext.getClass().getMethod("shutdown").invoke(springContext);
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
				System.out.println("Compiling: " + line);
			}
			process.waitFor();
			System.out.println("Recompile exit status " + process.exitValue());
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
		return dispatcherServlet;
	}

	public Object getSpringContext() {
		return springContext;
	}

}
