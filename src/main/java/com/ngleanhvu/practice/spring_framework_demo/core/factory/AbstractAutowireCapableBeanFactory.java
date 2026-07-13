package com.ngleanhvu.practice.spring_framework_demo.core.factory;

import com.ngleanhvu.practice.spring_framework_demo.core.annotation.Autowired;
import com.ngleanhvu.practice.spring_framework_demo.core.annotation.Order;
import com.ngleanhvu.practice.spring_framework_demo.core.model.BeanDefinition;
import com.ngleanhvu.practice.spring_framework_demo.core.processor.BeanPostProcessor;
import com.ngleanhvu.practice.spring_framework_demo.core.util.Util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.*;

public class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory {
    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();
    private final Deque<String> beansInCreation = new ArrayDeque<>();

    public void addBeanPostProcessor(BeanPostProcessor processor) {
        beanPostProcessors.add(processor);
    }

    public void sortBeanPostProcessors() {
        beanPostProcessors.sort(Comparator.comparingInt(this::getOrder));
    }

    private int getOrder(BeanPostProcessor processor) {
        Order order = processor.getClass().getAnnotation(Order.class);
        return order == null ? Integer.MAX_VALUE : order.value();
    }

    @Override
    protected Object createBean(BeanDefinition beanDefinition, String qualifierName) throws Exception {
        if (beanDefinition == null) {
            throw new RuntimeException("No bean definition found" +
                    (qualifierName != null ? " for qualifier '" + qualifierName + "'" : ""));
        }

        boolean singleton = beanDefinition.isSingleton();
        String beanName = beanDefinition.getBeanName();
        Class<?> targetClass = beanDefinition.getBeanClass();

        if (singleton && containsSingleton(beanName)) {
            return getSingleton(beanName);
        }
        if (!Util.isValidBean(targetClass)) {
            return null;
        }
        if (beansInCreation.contains(beanName)) {
            List<String> chain = new ArrayList<>(beansInCreation);
            chain.add(beanName);
            throw new RuntimeException("Circular dependency detected: " + String.join(" -> ", chain));
        }

        beansInCreation.push(beanName);
        Object bean;
        try {
            Constructor<?> constructor = getConstructor(targetClass);
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
            Object[] dependencies = new Object[parameterTypes.length];

            for (int i = 0; i < dependencies.length; i++) {
                Class<?> dependencyType = parameterTypes[i];
                String paramQualifier = Util.extractQualifier(parameterAnnotations[i]);
                BeanDefinition depDef = dependencyType.isInterface()
                        ? resolveType(dependencyType, paramQualifier)
                        : beanDefinitions.get(Util.getBeanName(dependencyType));

                if (depDef == null) {
                    throw new RuntimeException("No bean definition found for class " + dependencyType.getName());
                }
                Object dependency = createBean(depDef, paramQualifier);
                if (dependency == null) {
                    throw new RuntimeException("Dependency not found: " + dependencyType.getName());
                }
                dependencies[i] = dependency;
            }

            constructor.setAccessible(true);
            bean = constructor.newInstance(dependencies);

            for (BeanPostProcessor processor : beanPostProcessors) {
                bean = processor.postProcessBeforeInitialization(bean, beanName);
            }
            for (BeanPostProcessor processor : beanPostProcessors) {
                bean = processor.postProcessAfterInitialization(bean, beanName);
            }

            if (singleton) {
                registerSingleton(beanName, bean);
            }
        } finally {
            beansInCreation.pop();
        }
        return bean;
    }

    private Constructor<?> getConstructor(Class<?> clazz) throws NoSuchMethodException {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        Constructor<?> annotated = null;
        for (Constructor<?> c : constructors) {
            if (c.isAnnotationPresent(Autowired.class)) {
                if (annotated != null) throw new RuntimeException("Multiple constructors annotated with @Autowired in " + clazz.getName());
                annotated = c;
            }
        }
        if (annotated != null) return annotated;

        List<Constructor<?>> withParams = new ArrayList<>();
        for (Constructor<?> c : constructors) {
            if (c.getParameterCount() > 0) withParams.add(c);
        }
        if (withParams.size() > 1) throw new RuntimeException("Class " + clazz.getName() + " has multiple parameterized constructors.");
        if (withParams.size() == 1) return withParams.get(0);
        return clazz.getDeclaredConstructor();
    }


}
