package com.holybell.rpcfx.exception;

public class RpcfxException extends RuntimeException {

    public RpcfxException() {
    }

    public RpcfxException(String message) {
        super(message);
    }

    public RpcfxException(String messgae, Throwable e) {
        super(messgae, e);
    }

}
