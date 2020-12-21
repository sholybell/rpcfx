package com.holybell.rpcfx.nettyclient;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class AbstractClient implements EndPoint {

    private Logger logger = LoggerFactory.getLogger(AbstractClient.class);

    private String hostName;
    private int port;
    private int connectionTimeout;

    public AbstractClient(){
    }

    public AbstractClient(String hostName, int port, int connectionTimeout) {
        this.hostName = hostName;
        this.port = port;
        this.connectionTimeout = connectionTimeout;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName( String hostName) {
        this.hostName = hostName;
    }

    public int getPort() {
        return port;
    }

    public void setPort( int port) {
        this.port = port;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout( int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    protected final CountDownLatch countDownLatch = new CountDownLatch(1);
    protected String respMsg;

    public void send(Object message) throws Throwable {
        doOpen();
        doConnect();
        write(message);
    }

    public String receive() throws InterruptedException {
        boolean b = countDownLatch.await(getConnectionTimeout(), TimeUnit.MILLISECONDS);
        if (!b) {
            logger.error("Timeout(" + getConnectionTimeout() + "ms) when receiving response message");
        }
        return respMsg;
    }

    private void write(Object message) {
        Channel channel = getChannel();
        if (null != channel) {
            ChannelFuture f = channel.writeAndFlush(byteBufferFrom(message)).syncUninterruptibly();
            if (!f.isSuccess()) {
                logger.error("Failed to send message to " + getRemoteAddress() + f.cause().getMessage());
            }
        }
    }

    private ByteBuf byteBufferFrom(Object message) {
        return message instanceof String ? Unpooled.copiedBuffer((String) message, StandardCharsets.UTF_8) : Unpooled.copiedBuffer((byte[]) message);
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return new InetSocketAddress(hostName, port);
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        throw new NotImplementedException();
    }

    /**
     * Open client.
     *
     * @throws Throwable
     */
    protected abstract void doOpen() throws Throwable;

    /**
     * Connect to server.
     *
     * @throws Throwable
     */
    protected abstract void doConnect() throws Throwable;

    /**
     * Get the connected channel.
     *
     * @return channel
     */
    protected abstract Channel getChannel();

}