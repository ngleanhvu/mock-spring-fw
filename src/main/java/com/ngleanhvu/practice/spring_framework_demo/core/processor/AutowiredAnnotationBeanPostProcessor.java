package com.ngleanhvu.practice.spring_framework_demo.core.processor;

import com.ngleanhvu.practice.spring_framework_demo.core.*;
import com.ngleanhvu.practice.spring_framework_demo.core.annotation.Autowired;
import com.ngleanhvu.practice.spring_framework_demo.core.annotation.Component;
import com.ngleanhvu.practice.spring_framework_demo.core.annotation.Order;
import com.ngleanhvu.practice.spring_framework_demo.core.util.Util;

import java.lang.reflect.Field;

@Order(1)
@Component
public class AutowiredAnnotationBeanPostProcessor
        implements BeanPostProcessor {

    private final ApplicationContext context;

    public AutowiredAnnotationBeanPostProcessor(
            ApplicationContext context
    ) {
        this.context = context;
    }

    @Override
    public Object postProcessBeforeInitialization(
            Object bean,
            String beanName
    ) {

        for (Class<?> current = bean.getClass(); current != null; current = current.getSuperclass()) {
            Field[] fields =
                    bean.getClass()
                            .getDeclaredFields();

            for (Field field : fields) {

                if (field.isAnnotationPresent(Autowired.class)) {

                    try {

                        String qualifierName = Util.extractQualifier(field.getAnnotations());

                        Object dependency =
                                context.getBean(
                                        field.getType(),
                                        qualifierName
                                );

                        field.setAccessible(true);

                        field.set(
                                bean,
                                dependency
                        );

                    } catch (Exception e) {
                        throw new RuntimeException(
                                "Failed to autowire field '" + field.getName()
                                        + "' in " + bean.getClass().getName(), e);
                    }
                }
            }
        }

        return bean;
    }
}