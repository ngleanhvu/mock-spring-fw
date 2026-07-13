package com.ngleanhvu.practice.spring_framework_demo.core.factory;

public interface BeanFactory {
    <T> T getBean(Class<T> clazz);
    <T> T getBean(Class<T> clazz, String qualifier);
    Object getBean(String beanName);
    boolean containsBean(String beanName);
}