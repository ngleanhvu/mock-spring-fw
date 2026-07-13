package com.ngleanhvu.practice.spring_framework_demo.core.factory;

import com.ngleanhvu.practice.spring_framework_demo.core.model.BeanDefinition;
import com.ngleanhvu.practice.spring_framework_demo.core.util.Util;

import java.util.*;

public abstract class AbstractBeanFactory extends DefaultSingletonBeanRegistry  implements BeanFactory {
    protected final Map<String, BeanDefinition> beanDefinitions = new HashMap<>();
    protected final Map<Class<?>, List<BeanDefinition>> interfaceImplementations = new HashMap<>();

    @Override
    public <T> T getBean(Class<T> clazz) {
        return getBean(clazz, null);
    }

    @Override
    public <T> T getBean(Class<T> clazz, String qualifierName) {
        try {
            BeanDefinition beanDefinition = resolveType(clazz, qualifierName);
            String beanName = beanDefinition.getBeanName();

            Object bean = beanDefinition.isSingleton() ? getSingleton(beanName) : null;
            if (bean == null) {
                bean = createBean(beanDefinition, qualifierName);
            }
            return clazz.cast(bean);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object getBean(String beanName) {
        BeanDefinition def = beanDefinitions.get(beanName);
        if (def == null) throw new RuntimeException("No bean definition found: " + beanName);
        Object bean = def.isSingleton() ? getSingleton(beanName) : null;
        if (bean == null) {
            try {
                bean = createBean(def, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return bean;
    }

    @Override
    public boolean containsBean(String beanName) {
        return beanDefinitions.containsKey(beanName);
    }

    public void registerBeanDefinition(String beanName, BeanDefinition def) {
        beanDefinitions.put(beanName, def);
    }

    public void registerInterfaceImplementation(Class<?> iface, BeanDefinition def) {
        interfaceImplementations.computeIfAbsent(iface, k -> new ArrayList<>()).add(def);
    }

    protected BeanDefinition resolveType(Class<?> clazz, String qualifierName) {
        if (!clazz.isInterface()) {
            return beanDefinitions.get(Util.getBeanName(clazz));
        }
        List<BeanDefinition> implementations = interfaceImplementations.get(clazz);
        if (implementations == null || implementations.isEmpty()) {
            throw new RuntimeException("No implementation found for " + clazz.getName());
        }
        if (qualifierName != null) {
            return Util.getBeanByQualifier(implementations, qualifierName);
        }
        return getBeanDefinitionWithPrimaryAnnotation(implementations);
    }

    private BeanDefinition getBeanDefinitionWithPrimaryAnnotation(List<BeanDefinition> defs) {
        if (defs.size() == 1) return defs.get(0);
        BeanDefinition result = null;
        int countPrimary = 0;
        for (BeanDefinition def : defs) {
            if (def.isPrimary()) {
                if (countPrimary > 0) throw new RuntimeException("Multiple implementations found with @Primary annotation");
                result = def;
                countPrimary++;
            }
        }
        if (countPrimary == 0) throw new RuntimeException("Multiple implementations found but none marked @Primary and no @Qualifier specified");
        return result;
    }

    public Collection<BeanDefinition> getAllBeanDefinitions() {
        return beanDefinitions.values();
    }

    public BeanDefinition getBeanDefinition(String beanName) {
        return beanDefinitions.get(beanName);
    }

    public void clearBeanDefinitions() {
        beanDefinitions.clear();
        interfaceImplementations.clear();
    }

    protected abstract Object createBean(BeanDefinition beanDefinition, String qualifierName) throws Exception;

}