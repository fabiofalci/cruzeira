/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.netty;

import java.net.URL;
import java.net.URLClassLoader;

public class MyURLClassLoader extends URLClassLoader {

	public MyURLClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

}
