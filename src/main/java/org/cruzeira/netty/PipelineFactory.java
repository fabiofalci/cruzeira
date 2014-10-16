/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.cruzeira.server.ServerManager;

public class PipelineFactory extends ChannelInitializer<SocketChannel> {

    private ServerManager serverManager;
    private int asyncPoolSize;

    public PipelineFactory(int asyncPoolSize) {
        this.serverManager = new ServerManager();
        this.asyncPoolSize = asyncPoolSize;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        pipeline.addLast(new HttpServerCodec());

        pipeline.addLast(new HttpObjectAggregator(65536));
//        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new ServletChannelHandler(serverManager));
        pipeline.addLast(new DefaultEventExecutorGroup(asyncPoolSize), new AsyncServletChannelHandler(serverManager));
    }

    public void shutdown() {
        serverManager.getSpringContext().shutdown();
    }

}
