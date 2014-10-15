/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.cruzeira.netty.PipelineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point, starts Netty.
 */
public class Bootstrap {

    static PipelineFactory pipeline;

    static EventLoopGroup bossGroup = new NioEventLoopGroup();
    static EventLoopGroup workerGroup = new NioEventLoopGroup();

    public static void shutdown() {
        pipeline.shutdown();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("p", true, "Http port");
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

        int asyncPool;
        if (devMode) {
            asyncPool = 1;
        } else {
            asyncPool = cpus * 2 * 2;
        }

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new PipelineFactory(asyncPool));

        try {
            Channel ch = bootstrap.bind(port).sync().channel();
            logger.info("Running cruzeira {}...", port);
            ch.closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

}
