package com.holybell.rpcfx.nettyclient;


import java.net.InetSocketAddress;

public interface EndPoint {

    /**
     * Return  the local Inet address
     *
     * @return The local Inet address to which this <code>EndPoint</code> is bound, or <code>null</code>
     * if this <code>EndPoint</code> does not represent a network connection.
     */
    InetSocketAddress getLocalAddress();

    /**
     * Return the remote Inet address
     *
     * @return The remote Inet address to which this <code>EndPoint</code> is bound, or <code>null</code>
     * if this <code>EndPoint</code> does not represent a network connection.
     */
    InetSocketAddress getRemoteAddress();

}
