package com.holybell.rpcfx.proxy;

import com.holybell.rpcfx.errorhandler.RpcfxErrorHandler;

import java.lang.reflect.Proxy;

public class RpcClientProxyGenerator {

    public static <T> T getRpcClient(Class<T> rpcClientInterface, RpcfxErrorHandler errorHandler) {
        return (T) Proxy.newProxyInstance(rpcClientInterface.getClassLoader(),
                new Class[]{rpcClientInterface},
                new RpcClientInvocationHandler(rpcClientInterface, errorHandler));
    }
}
