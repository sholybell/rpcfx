package com.holybell.rpcfx.demo.provider;

import com.holybell.rpcfx.api.RpcfxResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class DemoResolver implements RpcfxResolver, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    // 通过使用接口类名获取实现类，避免强制定义Bean的名称来查找接口实现
    @Override
    public <T> T resolve(Class<T> requiredType) {
        return this.applicationContext.getBean(requiredType);
    }
}
