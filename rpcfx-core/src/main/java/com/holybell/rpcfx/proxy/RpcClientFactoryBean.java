package com.holybell.rpcfx.proxy;

import com.holybell.rpcfx.errorhandler.RpcfxErrorHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;

public class RpcClientFactoryBean<T> implements FactoryBean<T>, BeanFactoryAware {

    // 要生成实现类的接口
    private Class<T> rpcClientInterface;
    private BeanFactory beanFactory;

    public RpcClientFactoryBean(Class<T> rpcClientInterface) {
        this.rpcClientInterface = rpcClientInterface;
    }

    @Override
    public T getObject() throws Exception {
        return RpcClientProxyGenerator.getRpcClient(this.rpcClientInterface, beanFactory.getBean(RpcfxErrorHandler.class));
    }

    @Override
    public Class<T> getObjectType() {
        return this.rpcClientInterface;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
