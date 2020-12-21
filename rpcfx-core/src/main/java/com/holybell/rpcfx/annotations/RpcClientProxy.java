package com.holybell.rpcfx.annotations;

/**
 * 区别于 {@link RpcClient},这个接口用于自动生成代理对象，客户端无需实现API接口
 */
public @interface RpcClientProxy {
    String value() default "";
}
