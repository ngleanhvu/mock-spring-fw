package com.ngleanhvu.practice.spring_framework_demo.core.processor;


import com.ngleanhvu.practice.spring_framework_demo.core.annotation.Component;
import com.ngleanhvu.practice.spring_framework_demo.core.annotation.Order;
import com.ngleanhvu.practice.spring_framework_demo.core.annotation.Transactional;
import com.ngleanhvu.practice.spring_framework_demo.core.proxy.CglibTransactionalProxyFactory;

import java.lang.reflect.Method;

@Order(3)
@Component
public class TransactionalBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (hasTransactionalMethod(bean.getClass())) {

            return CglibTransactionalProxyFactory.createProxy(bean);

        }
        return bean;
    }

    private boolean hasTransactionalMethod(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Transactional.class)) {
                return true;
            }
        }
        return false;
    }
}