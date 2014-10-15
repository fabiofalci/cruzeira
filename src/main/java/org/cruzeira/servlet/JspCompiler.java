/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.servlet;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspC;

/**
 * Compiles .jsp to java classes
 */
public class JspCompiler {

    public JspCompiler(String uriRoot, String output) throws IOException {
        if (!Paths.get(uriRoot).toFile().exists()) {
            return;
        }

        JspC jspc = new JspC();
        jspc.setWebXmlFragment("/target/webfrag.xml");
        jspc.setUriroot(uriRoot);
        jspc.setPackage("views");
        jspc.setOutputDir(output);
        jspc.setValidateXml(false);
        jspc.setClassPath(null);
        jspc.setCompile(false);
        jspc.setSmapSuppressed(true);
        jspc.setSmapDumped(!true);
        jspc.setJavaEncoding("UTF-8");
        jspc.setTrimSpaces(false);
        jspc.setSystemClassPath(null);
        // jspc.setCompilerTargetVM("1.7");

        Path path = Paths.get(uriRoot);
        final List<String> fileNames = new ArrayList<>();
        final int remove = uriRoot.length() + 1;
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                if (path.toString().endsWith(".jsp")) {
                    fileNames.add(path.toString().substring(remove));
                }
                return super.visitFile(path, attrs);
            }
        });
        String jspFiles = StringUtils.join(fileNames, ",");
        jspc.setJspFiles(jspFiles);

        // try {
        // jspc.setIgnoreJspFragmentErrors(ignoreJspFragmentErrors);
        // } catch (NoSuchMethodError e) {
        // getLog().debug("Tomcat Jasper does not support configuration option 'ignoreJspFragmentErrors': ignored");
        // }

        // try {
        // if (schemaResourcePrefix != null)
        // jspc.setSchemaResourcePrefix(schemaResourcePrefix);
        // } catch (NoSuchMethodError e) {
        // getLog().debug("Tomcat Jasper does not support configuration option 'schemaResourcePrefix': ignored");
        // }
        // if (verbose)
        // jspc.setVerbose(99);
        // else
        // jspc.setVerbose(0);

        try {
            jspc.execute();
        } catch (JasperException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            new JspCompiler("./src/main/java/views", "./src/main/java");
            new JspCompiler("./src/test/java/views", "./src/test/java");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
