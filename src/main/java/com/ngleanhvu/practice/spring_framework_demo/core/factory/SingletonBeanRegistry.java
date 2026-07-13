package com.ngleanhvu.practice.spring_framework_demo.core.factory;

import java.util.Collection;

public interface SingletonBeanRegistry {
    void registerSingleton(String beanName, Object singletonObject);
    Object getSingleton(String beanName);
    boolean containsSingleton(String beanName);
    void removeSingleton(String beanName);
    void clearSingletons();
    Collection<Object> getSingletonBeansValue();
}