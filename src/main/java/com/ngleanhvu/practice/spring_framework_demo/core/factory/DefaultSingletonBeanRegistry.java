package com.ngleanhvu.practice.spring_framework_demo.core.factory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {
    private final Map<String, Object> singletonBeans = new HashMap<>();

    @Override
    public void registerSingleton(String beanName, Object singletonObject) {
        if (singletonBeans.containsKey(beanName)) {
            throw new RuntimeException("Singleton bean already exists: " + beanName);
        }
        singletonBeans.put(beanName, singletonObject);
    }

    @Override
    public Object getSingleton(String beanName) {
        return singletonBeans.get(beanName);
    }

    @Override
    public boolean containsSingleton(String beanName) {
        return singletonBeans.containsKey(beanName);
    }

    @Override
    public void removeSingleton(String beanName) {
        singletonBeans.remove(beanName);
    }

    @Override
    public void clearSingletons() {
        singletonBeans.clear();
    }

    @Override
    public Collection<Object> getSingletonBeansValue() {
        return singletonBeans.values();
    }
}