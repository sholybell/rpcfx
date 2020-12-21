package com.holybell.rpcfx.errorhandler;

@FunctionalInterface
public interface RpcfxErrorHandler {
    Object handleError(Throwable throwable);
}
