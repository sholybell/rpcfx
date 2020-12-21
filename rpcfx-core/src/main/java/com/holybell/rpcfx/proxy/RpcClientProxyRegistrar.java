package com.holybell.rpcfx.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

public class RpcClientProxyRegistrar implements ImportBeanDefinitionRegistrar, BeanFactoryAware {

    private Logger logger = LoggerFactory.getLogger(RpcClientProxyRegistrar.class);

    @Autowired
    private BeanFactory beanFactory;    // Spring内部会自动注入这个对象

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        ClassPathRpcClientProxyScanner scanner = new ClassPathRpcClientProxyScanner(registry);
//        List<String> packages = AutoConfigurationPackages.get(this.beanFactory);
        // 扫描注册BeanDefinition
        scanner.doScan("com.holybell.rpcfx.demo.api");
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
