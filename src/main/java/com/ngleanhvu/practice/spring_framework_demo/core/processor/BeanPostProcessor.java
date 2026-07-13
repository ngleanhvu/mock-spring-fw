package com.ngleanhvu.practice.spring_framework_demo.core.processor;

import java.lang.reflect.InvocationTargetException;

public interface BeanPostProcessor {
    default Object postProcessBeforeInitialization(
            Object bean,
            String beanName
    ) throws InvocationTargetException, IllegalAccessException {
        return bean;
    }

    default Object postProcessAfterInitialization(
            Object bean,
            String beanName
    ) throws InvocationTargetException, IllegalAccessException {
        return bean;
    }

}
