package controllers;

import java.util.concurrent.Callable;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MyController {

	private SessionFactory sessionFactory;

	final Logger logger = LoggerFactory.getLogger(MyController.class);

	@RequestMapping("/async")
	public Callable<String> async(final Model model) {
		logger.info("Starting async");
		return new Callable<String>() {
			@Override
			public String call() throws Exception {
				logger.info("Running async....");
				return "async";
			}
		};
	}

	@RequestMapping("/controller")
	public String controller(Model model) {
		logger.info("Running controller...");
		return "controller";
	}
	
	@RequestMapping("/scriptlets")
	public String scriptlets(Model model) {
		return "scriptlet";
	}

}
