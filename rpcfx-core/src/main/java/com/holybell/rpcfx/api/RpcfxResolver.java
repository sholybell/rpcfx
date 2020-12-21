package com.holybell.rpcfx.api;

public interface RpcfxResolver {

    <T> T resolve(Class<T> requiredType);
}