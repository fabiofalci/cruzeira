/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.server;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.cruzeira.netty.PipelineFactory;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point, starts Netty.
 */
public class Bootstrap {
	
	static ServerBootstrap bootstrap;
	
	public static void shutdown() {
		bootstrap.shutdown();
	}

	/**
	 * There are 2 arguments: '-p 8080' for port and '-dev' for development mode
	 */
	public static void main(String[] args) {
		Options options = new Options();
		options.addOption("p", true, "Http port");
		options.addOption("dev", false, "Development mode");
		CommandLineParser parser = new BasicParser();
		Logger logger = LoggerFactory.getLogger(Bootstrap.class);
		int port = 8080;
		boolean devMode = false;
		try {
			CommandLine cmd = parser.parse(options, args);	
			if (cmd.hasOption("p")) {
				try {
					port = Integer.valueOf(cmd.getOptionValue("p"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			devMode = cmd.hasOption("dev");
			if (devMode) {
				logger.info("Running in development mode");
			} else {
				logger.info("Running in normal mode");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Error on cli");
		}
		
		int cpus = Runtime.getRuntime().availableProcessors();
		logger.info("CPUs: {}", cpus);

		ChannelFactory factory;
		int asyncPool; 
		if (devMode) {
			asyncPool = 1;
			factory = new NioServerSocketChannelFactory(Executors.newSingleThreadExecutor(), Executors.newSingleThreadExecutor(), 1);
		} else {
			asyncPool = cpus * 2 * 2;
			factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
		}
		
		OrderedMemoryAwareThreadPoolExecutor eventExecutor = new OrderedMemoryAwareThreadPoolExecutor(asyncPool, 0, 0, 30, TimeUnit.SECONDS);

		bootstrap = new ServerBootstrap(factory);
		new OpenWebJar();

		bootstrap.setPipelineFactory(new PipelineFactory(eventExecutor, devMode));

		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", false);
		bootstrap.bind(new InetSocketAddress(port));
		logger.info("Running cruzeira {}...", port);
	}
	
}
