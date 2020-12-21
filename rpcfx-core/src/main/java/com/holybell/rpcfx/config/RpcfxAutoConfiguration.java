package com.holybell.rpcfx.config;

import com.holybell.rpcfx.proxy.RpcClientProxyRegistrar;
import com.holybell.rpcfx.aspects.RpcProxyFactoryAspect;
import com.holybell.rpcfx.errorhandler.DefaultRpxfxErrorHandler;
import com.holybell.rpcfx.errorhandler.RpcfxErrorHandler;
import com.holybell.rpcfx.registry.RpcServerRegister;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
public class RpcfxAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RpcfxErrorHandler rpcfxErrorHandler() {
        return new DefaultRpxfxErrorHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    // 必须有提供此属性才会加载AOP切面类
    @ConditionalOnProperty(prefix = "rpcfx.aspect", value = "enabled", havingValue = "true", matchIfMissing = false)
    public RpcProxyFactoryAspect rpcProxyFactoryAspect() {
        return new RpcProxyFactoryAspect();
    }

    /**
     * 判断类是否存在@RpcServer注解
     * TODO cnsumer工程这个bean是多余的，加参数控制是否注册Bean?
     */
    @Bean
    public RpcServerRegister rpcServerRegister() {
        return new RpcServerRegister();
    }

    /**
     * 自动化装配标注了@RpcClientProxy注解的类
     */
    @Configuration
    // 未启动Spring切面的情况下生效 , 同时 provider工程不加载此配置
    @ConditionalOnMissingBean(type = {"com.holybell.rpcfx.aspects.RpcProxyFactoryAspect", "com.holybell.rpcfx.demo.provider.config.ProviderMarker"})
    @Import(RpcClientProxyRegistrar.class)
    public static class RpcClientProxyAutoConfiguration {

    }
}
