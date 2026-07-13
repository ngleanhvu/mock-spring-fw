package com.ngleanhvu.practice.spring_framework_demo.core.processor;

import com.ngleanhvu.practice.spring_framework_demo.core.annotation.Component;
import com.ngleanhvu.practice.spring_framework_demo.core.annotation.Order;

@Order(3)
@Component
public class LoggingBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(
            Object bean,
            String beanName
    ) {

        System.out.println(
                "Before init: " + beanName
        );

        return bean;
    }


    @Override
    public Object postProcessAfterInitialization(
            Object bean,
            String beanName
    ) {

        System.out.println(
                "After init: " + beanName
        );

        return bean;
    }
}
