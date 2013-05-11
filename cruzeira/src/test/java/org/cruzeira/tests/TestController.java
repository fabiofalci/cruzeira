/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.tests;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {

	@RequestMapping("/simple")
	public @ResponseBody String simple() {
		return "simple";
	}

	@RequestMapping("/jsp")
	public String jsp() {
		return "test";
	}
	
	@RequestMapping("/jspWithInclude")
	public String jspWithInclude() {
		return "sub/sub";
	}

	@RequestMapping("/async")
	public Callable<String> async() {
		return new Callable<String>() {
			@Override
			public String call() throws Exception {
				return "test";
			}
		};
	}
	
	@RequestMapping("/runtimeException")
	public String runtimeException() {
		throw new RuntimeException("On purpose");
	}
	
	@RequestMapping("/exception")
	public String exception() throws Exception {
		throw new Exception("On purpose");
	}
	
	@RequestMapping("/error") 
	public void error(HttpServletResponse response) {
		try {
			response.sendError(501, "On purpose: Not Implemented");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping("/printWriter") 
	public void printWriter(HttpServletResponse response) {
		try {
			response.setContentType("text/html;charset=UTF-8");
			PrintWriter writer = response.getWriter();
			writer.append("Using print writer response");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
