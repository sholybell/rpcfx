package com.holybell.rpcfx.proxy;

import com.holybell.rpcfx.annotations.RpcClientProxy;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Set;

public class ClassPathRpcClientProxyScanner extends ClassPathBeanDefinitionScanner {

    ClassPathRpcClientProxyScanner(BeanDefinitionRegistry registry) {
        super(registry);
        // 指定扫描的类
        addIncludeFilter(new AnnotationTypeFilter(RpcClientProxy.class));
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitionHolders = super.doScan(basePackages);
        for (BeanDefinitionHolder holder : beanDefinitionHolders) {
            GenericBeanDefinition beanDefinition = (GenericBeanDefinition) holder.getBeanDefinition();
            beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
            beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(beanDefinition.getBeanClassName());
            beanDefinition.setBeanClass(RpcClientFactoryBean.class);
        }
        return beanDefinitionHolders;
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
    }
}
