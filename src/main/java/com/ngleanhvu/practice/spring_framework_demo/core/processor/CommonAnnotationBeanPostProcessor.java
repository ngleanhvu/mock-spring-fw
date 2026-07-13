package com.ngleanhvu.practice.spring_framework_demo.core.processor;

import com.ngleanhvu.practice.spring_framework_demo.core.annotation.Component;
import com.ngleanhvu.practice.spring_framework_demo.core.annotation.Order;
import com.ngleanhvu.practice.spring_framework_demo.core.annotation.PostConstruct;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

@Order(2)
@Component
public class CommonAnnotationBeanPostProcessor implements BeanPostProcessor {
    private final Set<Object> initializedBeans = new HashSet<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws InvocationTargetException, IllegalAccessException {
        if (initializedBeans.contains(bean)) {
            return bean;
        }
        for (Class<?> current = bean.getClass(); current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.isAnnotationPresent(PostConstruct.class) && method.getParameterCount() == 0) {
                    method.setAccessible(true);
                    method.invoke(bean);
                }
            }
        }

        initializedBeans.add(bean);
        return bean;

    }

}
