/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.server;


public class OpenWebJar {

    public OpenWebJar() {
        // FIXME auto discover webjar and open it
//		String file = "/META-INF/resources/webjars/bootstrap/2.3.0/css/bootstrap.min.css";
//		URL url = getClass().getResource(file);
//		String path = url.getFile();
//		String jarPath = path.substring(0, path.indexOf("!"));
//		try {
//			extractJar(new File(jarPath));
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//
//		List<String> webJars = Arrays.asList("");

        // new
        // File(MyClass.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    }

//	private void extractJar(File jarFile) throws Exception {
//		String destDir = "target/webjars/";
//		java.util.jar.JarFile jar = new java.util.jar.JarFile(jarFile);
//		java.util.Enumeration enum1 = jar.entries();
//		while (enum1.hasMoreElements()) {
//		    java.util.jar.JarEntry file = (java.util.jar.JarEntry) enum1.nextElement();
//		    java.io.File f = new java.io.File(destDir + java.io.File.separator + file.getName());
//		    if (file.isDirectory()) { // if its a directory, create it
//		    	f.mkdir();
//		    	continue;
//		    }
//		    java.io.InputStream is = jar.getInputStream(file); // get the input stream
//		    java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
//		    while (is.available() > 0) {  // write contents of 'is' to 'fos'
//		    	fos.write(is.read());
//		    }
//		    fos.close();
//		    is.close();
//		}		
//	}
}
