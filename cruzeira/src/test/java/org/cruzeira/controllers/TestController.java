/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.controllers;

import java.util.concurrent.Callable;

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

	@RequestMapping("/async")
	public Callable<String> async() {
		return new Callable<String>() {
			@Override
			public String call() throws Exception {
				return "test";
			}
		};
	}

}
