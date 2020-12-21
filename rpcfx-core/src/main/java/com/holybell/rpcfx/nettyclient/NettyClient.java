package com.holybell.rpcfx.nettyclient;


import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class NettyClient extends AbstractClient {

    private Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private Bootstrap bootstrap;

    private Channel channel;

    private static final NioEventLoopGroup NIO_GROUP = new NioEventLoopGroup();

    public NettyClient(String hostName, int port, int connectionTimeout) {
        super(hostName, port, connectionTimeout);
    }

    private class ClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            channel = ctx.channel();
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
            try {
                respMsg = msg.toString(StandardCharsets.UTF_8);
            } finally {
                countDownLatch.countDown();
                ctx.close();
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            logger.error("An exception was thrown, cause: {}", cause.getMessage(), cause);
            ctx.close();
        }
    }

    @Override
    protected void doOpen() {
        bootstrap = new Bootstrap();
        bootstrap
                .group(NIO_GROUP)
                .remoteAddress(getRemoteAddress())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, getConnectionTimeout())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new ClientHandler());
                    }
                });
    }

    @Override
    public void doConnect() {
        ChannelFuture f = bootstrap.connect().syncUninterruptibly();
        if (!f.isSuccess() && null != f.cause()) {
            logger.error("The client failed to connect the server:{},error message is:{}", getRemoteAddress(), f.cause().getMessage());
        }
    }

    @Override
    protected Channel getChannel() {
        return channel;
    }

}
