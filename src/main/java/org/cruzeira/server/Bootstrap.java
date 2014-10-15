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

    final static Logger logger = LoggerFactory.getLogger(Bootstrap.class);

    private PipelineFactory pipeline;

    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    private static Bootstrap bootstrap;

    public static void shutdownNow() {
        bootstrap.shutdown();
    }

    public void shutdown() {
        pipeline.shutdown();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    public Channel start(int port, int availableProcessors) {
        int asyncPool = availableProcessors * 2 * 2;
        pipeline = new PipelineFactory(asyncPool);
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(pipeline);

        try {
            Channel channel = serverBootstrap.bind(port).sync().channel();
            logger.info("Running cruzeira {}...", port);
            return channel;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("p", true, "Http port");
        CommandLineParser parser = new BasicParser();
        int port = 8080;
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("p")) {
                try {
                    port = Integer.valueOf(cmd.getOptionValue("p"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Error on cli");
        }

        int availableProcessors = Runtime.getRuntime().availableProcessors();
        logger.info("Processors: {}", availableProcessors);

        bootstrap = new Bootstrap();
        try {
            Channel channel = bootstrap.start(port, availableProcessors);
            channel.closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shutdownNow();
        }
    }

}
