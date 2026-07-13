package com.ngleanhvu.practice.spring_framework_demo.core;

import com.ngleanhvu.practice.spring_framework_demo.core.annotation.PreDestroy;
import com.ngleanhvu.practice.spring_framework_demo.core.annotation.Primary;
import com.ngleanhvu.practice.spring_framework_demo.core.factory.AbstractAutowireCapableBeanFactory;
import com.ngleanhvu.practice.spring_framework_demo.core.model.BeanDefinition;
import com.ngleanhvu.practice.spring_framework_demo.core.processor.BeanPostProcessor;
import com.ngleanhvu.practice.spring_framework_demo.core.util.PackageScanner;
import com.ngleanhvu.practice.spring_framework_demo.core.util.Util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public final class ApplicationContext {

    private final AbstractAutowireCapableBeanFactory beanFactory = new AbstractAutowireCapableBeanFactory();

    public ApplicationContext(String packageName) {
        try {
            PackageScanner scanner = new PackageScanner();
            Set<Class<?>> classes = scanner.scan(packageName);

            List<Class<?>> postProcessorClasses = new ArrayList<>();

            for (Class<?> clazz : classes) {
                if (clazz.isAnnotation() || clazz.isInterface()) {
                    continue;
                }

                if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                    postProcessorClasses.add(clazz);
                    continue;
                }

                if (Util.isValidBean(clazz)) {
                    createBeanDefinition(clazz);
                }
            }

            buildInterfacesMap();

            for (Class<?> ppClass : postProcessorClasses) {
                registerBeanPostProcessor(ppClass);
            }

            beanFactory.sortBeanPostProcessors();

            for (BeanDefinition beanDefinition : new ArrayList<>(beanFactory.getAllBeanDefinitions())) {
                if (beanDefinition.isSingleton()) {
                    beanFactory.getBean(beanDefinition.getBeanClass());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T getBean(Class<T> clazz) {
        return beanFactory.getBean(clazz);
    }

    public <T> T getBean(Class<T> clazz, String qualifierName) {
        return beanFactory.getBean(clazz, qualifierName);
    }

    public Object getBean(String beanName) {
        return beanFactory.getBean(beanName);
    }

    public void close() throws InvocationTargetException, IllegalAccessException {
        for (Object bean : beanFactory.getSingletonBeansValue()) {
            destroyBeans(bean);
        }
        beanFactory.clearBeanDefinitions();
        beanFactory.clearSingletons();
    }

    private void createBeanDefinition(Class<?> clazz) {
        boolean hasPrimaryAnnotation = clazz.isAnnotationPresent(Primary.class);
        String beanName = Util.getBeanName(clazz);
        String scope = Util.getScopeOfBeanDefinition(clazz);
        boolean isSingleton = Util.isSingleton(scope);

        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setBeanClass(clazz);
        beanDefinition.setBeanName(beanName);
        beanDefinition.setPrimary(hasPrimaryAnnotation);
        beanDefinition.setSingleton(isSingleton);

        beanFactory.registerBeanDefinition(beanName, beanDefinition);
    }

    private void buildInterfacesMap() {
        for (BeanDefinition beanDefinition : beanFactory.getAllBeanDefinitions()) {
            Class<?> clazz = beanDefinition.getBeanClass();
            if (!Util.isValidBean(clazz)) {
                continue;
            }
            for (Class<?> iface : Util.getAllInterfaces(clazz)) {
                beanFactory.registerInterfaceImplementation(iface, beanDefinition);
            }
        }
    }

    private void registerBeanPostProcessor(Class<?> clazz) {
        try {
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            Constructor<?> chosen = null;

            for (Constructor<?> constructor : constructors) {
                if (constructor.getParameterCount() == 0) {
                    chosen = constructor;
                    break;
                }
            }
            if (chosen == null) {
                for (Constructor<?> constructor : constructors) {
                    Class<?>[] paramTypes = constructor.getParameterTypes();
                    if (paramTypes.length == 1 && paramTypes[0].isAssignableFrom(ApplicationContext.class)) {
                        chosen = constructor;
                        break;
                    }
                }
            }
            if (chosen == null) {
                throw new RuntimeException(
                        "Unsupported constructor for BeanPostProcessor: " + clazz.getName()
                                + ". Only no-arg constructors or a single-arg (ApplicationContext) constructor are supported.");
            }

            chosen.setAccessible(true);
            Object object = chosen.getParameterCount() == 0
                    ? chosen.newInstance()
                    : chosen.newInstance(this);

            beanFactory.addBeanPostProcessor((BeanPostProcessor) object);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void destroyBeans(Object bean) throws InvocationTargetException, IllegalAccessException {
        for (Class<?> current = bean.getClass(); current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.isAnnotationPresent(PreDestroy.class) && method.getParameterCount() == 0) {
                    method.setAccessible(true);
                    method.invoke(bean);
                }
            }
        }
    }

}