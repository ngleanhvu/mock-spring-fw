package com.ngleanhvu.practice.spring_framework_demo.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public final class ApplicationContextV1 {
    private final Map<String, BeanDefinition> beanDefinitions = new HashMap<>();
    private final Map<String, Object> singletonBeans = new HashMap<>();
    private final Map<Class<?>, List<BeanDefinition>> interfaceImplementations = new HashMap<>();
    private final Set<Object> initializedBeans = new HashSet<>();

    public ApplicationContextV1(String packageName) {
        try {
            PackageScanner scanner = new PackageScanner();

            Set<Class<?>> classes = scanner.scan(packageName);


            for (Class<?> clazz : classes) {
                if (clazz.isAnnotation() || clazz.isInterface() || !Util.isValidBean(clazz)) {
                    continue;
                }
                createBeanDefinition(clazz);
            }

            buildInterfacesMap(beanDefinitions);

            for (BeanDefinition beanDefinition : beanDefinitions.values()) {
                createBean(beanDefinition);
            }

            injectDependencies();
            invokeInitMethods();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

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

        beanDefinitions.put(beanName, beanDefinition);

    }


    public void close() throws InvocationTargetException, IllegalAccessException {
        this.destroyBeans();
        initializedBeans.clear();
        interfaceImplementations.clear();
    }

    private void createBean(BeanDefinition beanDefinition) throws
            InvocationTargetException,
            NoSuchMethodException,
            InstantiationException,
            IllegalAccessException {
        createBean(beanDefinition, null);
    }

    private Object createBean(BeanDefinition beanDefinition, String qualifierName) throws
            NoSuchMethodException,
            InvocationTargetException,
            InstantiationException,
            IllegalAccessException {

        BeanDefinition beanDefinition1 = resolveType(beanDefinition.getBeanClass(), qualifierName);
        String beanName = Util.getBeanName(beanDefinition1.getBeanClass());
        Class<?> targetClass = beanDefinition1.getBeanClass();
        if (singletonBeans.containsKey(beanName)) {
            return singletonBeans.get(beanName);
        }

        if (!Util.isValidBean(targetClass)) {
            return null;
        }

        Constructor<?> constructor = getConstructor(targetClass);

        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();

        Object[] dependencies = new Object[parameterTypes.length];

        for (int i = 0; i < dependencies.length; i++) {
            Class<?> dependencyType = parameterTypes[i];
            String beanName1 = Util.getBeanName(dependencyType);
            BeanDefinition beanDef = beanDefinitions.get(beanName1);
            String paramQualifier = Util.extractQualifier(parameterAnnotations[i]);
            Object dependency = createBean(beanDef, paramQualifier);
            if (dependency == null) {
                throw new RuntimeException("Dependency not found: " + dependencyType.getName());
            }
            dependencies[i] = dependency;
        }

        constructor.setAccessible(true);
        Object bean = constructor.newInstance(dependencies);

        registerBean(beanDefinition, bean);

        return bean;
    }

    private void registerBean(BeanDefinition beanDefinition, Object bean) {

        String beanName = beanDefinition.getBeanName();
        singletonBeans.put(beanName, bean);

    }

    private void injectDependencies() throws InvocationTargetException,
            NoSuchMethodException,
            InstantiationException,
            IllegalAccessException {
        for (Object bean : singletonBeans.values()) {
            Field[] fields = bean.getClass().getDeclaredFields();
            for (Field field : fields) {
                boolean hasAutowired = field.isAnnotationPresent(Autowired.class);
                if (hasAutowired) {
                    String qualifierName = field.isAnnotationPresent(Qualifier.class)
                            ? field.getAnnotation(Qualifier.class).value()
                            : null;
                    BeanDefinition beanDefinition = getBeanDefinitionByClass(field.getType());
                    Object dependency = createBean(beanDefinition, qualifierName);
                    if (dependency == null) {
                        throw new RuntimeException(
                                "Dependency not found: "
                                        + field.getType().getName()
                        );
                    }
                    field.setAccessible(true);
                    field.set(bean, dependency);
                }
            }
        }
    }

    private void invokeInitMethods() throws InvocationTargetException, IllegalAccessException {
        for (Object bean : singletonBeans.values()) {
            if (initializedBeans.contains(bean)) {
                continue;
            }
            Method[] methods = bean.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(PostConstruct.class) && method.getParameterCount() == 0) {
                    method.setAccessible(true);
                    method.invoke(bean);
                }
            }
            initializedBeans.add(bean);
        }
    }

    private void destroyBeans() throws InvocationTargetException, IllegalAccessException {
        for (Object bean : singletonBeans.values()) {
            Method[] methods = bean.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(PreDestroy.class) && method.getParameterCount() == 0) {
                    method.setAccessible(true);
                    method.invoke(bean);
                }
            }
        }
    }

    private void buildInterfacesMap(Map<String, BeanDefinition> beanDefinitions) {
        for (BeanDefinition beanDefinition : beanDefinitions.values()) {
            Class<?> clazz = beanDefinition.getBeanClass();
            if (!Util.isValidBean(clazz)) {
                continue;
            }
            for (Class<?> iface : getAllInterfaces(clazz)) {
                interfaceImplementations
                        .computeIfAbsent(iface, k -> new ArrayList<>())
                        .add(beanDefinition);
            }
        }
    }


    private Constructor<?> getConstructor(Class<?> clazz) throws NoSuchMethodException {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() > 0) {
                return constructor;
            }
        }
        return clazz.getDeclaredConstructor();
    }

    private BeanDefinition resolveType(Class<?> clazz, String qualifierName) {
        if (!clazz.isInterface()) {
            return getBeanDefinitionByClass(clazz);
        }

        List<BeanDefinition> implementations = interfaceImplementations.get(clazz);

        if (implementations == null || implementations.isEmpty()) {
            throw new RuntimeException(
                    "No implementation found for " + clazz.getName()
            );
        }

        if (qualifierName != null) {
            return Util.getBeanByQualifier(implementations, qualifierName);
        }

        return getBeaDefinitionWithPrimaryAnnotation(implementations);
    }

    private BeanDefinition getBeaDefinitionWithPrimaryAnnotation(List<BeanDefinition> beanDefinitions) {
        if (beanDefinitions.size() == 1) return beanDefinitions.get(0);
        int countPrimary = 0;
        BeanDefinition result = null;
        for (BeanDefinition beanDefinition : beanDefinitions) {
            if (beanDefinition.isPrimary() && countPrimary > 0) {
                throw new RuntimeException("Multiple implementations found has @Primary annotation");
            }
            result = beanDefinition;
            countPrimary++;
        }

        if (countPrimary == 0) {
            throw new RuntimeException(
                    "Multiple implementations found for "
                            + " but none marked @Primary and no @Qualifier specified"
            );
        }

        return result;
    }

    public <T> T getBean(Class<T> clazz) {
        return getBean(clazz, null);
    }

    public <T> T getBean(Class<T> clazz, String qualifierName) {
        try {

            BeanDefinition beanDefinition = resolveType(clazz, qualifierName);
            String beanName = Util.getBeanName(clazz);
            Object bean = singletonBeans.get(beanName);

            if (bean == null) {
                bean = createBean(beanDefinition, qualifierName);
            }

            return clazz.cast(bean);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // private util

    private BeanDefinition getBeanDefinitionByClass(Class<?> clazz) {
        String beanName = Util.getBeanName(clazz);
        return beanDefinitions.get(beanName);
    }

    private Set<Class<?>> getAllInterfaces(Class<?> clazz) {
        Set<Class<?>> result = new HashSet<>();
        Class<?> current = clazz;
        while (current != null) {
            Class<?>[] interfaces = current.getInterfaces();
            for (Class<?> iface : interfaces) {
                if (result.add(iface)) {
                    result.addAll(getAllInterfaces(iface));
                }
            }
            current = current.getSuperclass();
        }
        return result;
    }
}
