/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.plugin.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.cruzeira.servlet.JspCompiler;

@Mojo(name = "run")
public class RunMojo extends AbstractMojo {

	public void execute() throws MojoExecutionException {
		getLog().info("Running JSP compiler"); 
		JspCompiler.main(null);
		getLog().info("JSP compiler done"); 
	}
}
