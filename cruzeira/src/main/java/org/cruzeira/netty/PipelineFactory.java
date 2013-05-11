/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.netty;

import java.util.concurrent.Executor;

import org.cruzeira.server.ServerManager;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;

public class PipelineFactory implements ChannelPipelineFactory {
	private ServerManager serverManager;
	private Executor pipelineExecutor;
	private boolean devMode;

	public PipelineFactory(Executor executor, boolean devMode) {
		this.serverManager = new ServerManager();
		this.pipelineExecutor = executor;
		this.devMode = devMode;
	}

	public ChannelPipeline getPipeline() {
		// Create a default pipeline implementation.
		ChannelPipeline pipeline = Channels.pipeline();

		// Uncomment the following line if you want HTTPS
		// SSLEngine engine =
		// SecureChatSslContextFactory.getServerContext().createSSLEngine();
		// engine.setUseClientMode(false);
		// pipeline.addLast("ssl", new SslHandler(engine));

		pipeline.addLast("decoder", new HttpRequestDecoder());
		// Uncomment the following line if you don't want to handle
		// HttpChunks.
		pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
		pipeline.addLast("encoder", new HttpResponseEncoder());
		// Remove the following line if you don't want automatic content
		// compression.
		// pipeline.addLast("deflater", new HttpContentCompressor());
		pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
		pipeline.addLast("filehandler", new ResourcesChannelHandler());

		pipeline.addLast("handler", new ServletChannelHandler(serverManager, devMode));
		pipeline.addLast("pipelineExecutor", new ExecutionHandler(pipelineExecutor));
		pipeline.addLast("asyncHandler", new AsyncServletChannelHandler(serverManager));
		return pipeline;
	}

}
