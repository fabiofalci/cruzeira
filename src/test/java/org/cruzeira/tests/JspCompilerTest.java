/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.cruzeira.servlet.JspCompiler;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

/**
 * Very basic test to check if the .java files are created, just that.
 */
public class JspCompilerTest {

    @AfterClass
    public static void clean() {
        try {
            Files.deleteIfExists(Paths.get("./target/jspcompiler/views/test_jsp.java"));
            Files.deleteIfExists(Paths.get("./target/jspcompiler/views"));
            Files.deleteIfExists(Paths.get("./target/jspcompiler"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void compileJsp() {
        try {
            new JspCompiler("./src/test/java/views/compiler", "./target/jspcompiler");
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        Assert.assertTrue(Paths.get("./target/jspcompiler/views/test_jsp.java").toFile().exists());
    }
}
