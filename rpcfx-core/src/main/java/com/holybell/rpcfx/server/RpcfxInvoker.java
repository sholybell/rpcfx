package com.holybell.rpcfx.server;

import com.holybell.rpcfx.api.RpcfxRequest;
import com.holybell.rpcfx.api.RpcfxResolver;
import com.holybell.rpcfx.api.RpcfxResponse;
import com.holybell.rpcfx.exception.RpcfxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

public class RpcfxInvoker {

    private Logger logger = LoggerFactory.getLogger(RpcfxInvoker.class);

    private RpcfxResolver resolver;

    public RpcfxInvoker(RpcfxResolver resolver) {
        this.resolver = resolver;
    }

    public <T> RpcfxResponse invoke(RpcfxRequest<T> request) {
        RpcfxResponse response = new RpcfxResponse();
        Class<T> serviceClass = request.getServiceClass();
        try {
            // 作业1：改成泛型和反射
            T service = resolver.resolve(serviceClass);
            Method method = resolveMethodFromClass(service.getClass(), request.getMethod());
            if (Optional.ofNullable(method).isPresent()) {
                Object result = method.invoke(service, request.getParams()); // dubbo, fastjson,
                // 两次json序列化能否合并成一个
                response.setResult(result);
                response.setStatus(true);
                return response;
            }
            throw new RpcfxException("target method " + request.getMethod() + " not found!");
        } catch (Exception e) {

            // 3.Xstream

            // 2.封装一个统一的RpcfxException,客户端也需要判断异常
            if (logger.isWarnEnabled()) {
                logger.warn("RPC 服务端调用异常", e);
            }
            response.setException(new RpcfxException("PRC ERROR", e.getCause()));
            response.setStatus(false);
            return response;
        }
    }

    private Method resolveMethodFromClass(Class<?> klass, String methodName) {
        Optional<Method> method = Arrays.stream(klass.getMethods()).filter(m -> methodName.equals(m.getName())).findFirst();
        return method.orElse(null);
    }
}
