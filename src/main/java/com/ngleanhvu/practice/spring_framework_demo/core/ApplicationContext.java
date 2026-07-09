package com.ngleanhvu.practice.spring_framework_demo.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public final class ApplicationContext {
    private final Map<Class<?>, Object> singletonBeans = new HashMap<>();
    private final Map<Class<?>, List<Class<?>>> interfaceImplementations = new HashMap<>();
    private final Map<String, Object> beanByName = new HashMap<>();

    public ApplicationContext(String packageName) {
        try {
            PackageScanner scanner = new PackageScanner();

            Set<Class<?>> classes = scanner.scan(packageName);

            buildInterfacesMap(classes);

            for (Class<?> clazz : classes) {
                if (clazz.isInterface() || clazz.isAnnotation()) {
                    continue;
                }
                createBean(clazz);
            }

            injectDependencies();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void createBean(Class<?> clazz) throws
            InvocationTargetException,
            NoSuchMethodException,
            InstantiationException,
            IllegalAccessException {
        createBean(clazz, null);
    }

    private Object createBean(Class<?> clazz, String qualifierName) throws
            NoSuchMethodException,
            InvocationTargetException,
            InstantiationException,
            IllegalAccessException {

        Class<?> targetClass = resolveType(clazz, qualifierName);

        if (singletonBeans.containsKey(targetClass)) {
            return singletonBeans.get(targetClass);
        }

        if (!isValidBean(targetClass)) {
            return null;
        }

        Constructor<?> constructor = getConstructor(targetClass);

        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();

        Object[] dependencies = new Object[parameterTypes.length];

        for (int i = 0; i < dependencies.length; i++) {
            Class<?> dependencyType = parameterTypes[i];
            String paramQualifier = extractQualifier(parameterAnnotations[i]);
            Object dependency = createBean(dependencyType, paramQualifier);
            if (dependency == null) {
                throw new RuntimeException("Dependency not found: " + dependencyType.getName());
            }
            dependencies[i] = dependency;
        }

        constructor.setAccessible(true);
        Object bean = constructor.newInstance(dependencies);

        registerBean(targetClass, bean);

        return bean;
    }

    private String extractQualifier(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof Qualifier) {
                return ((Qualifier) annotation).value();
            }
        }
        return null;
    }

    private void registerBean(Class<?> clazz, Object bean) {

        singletonBeans.put(clazz, bean);


        if(clazz.isAnnotationPresent(Qualifier.class)){
            String name =
                    clazz.getAnnotation(Qualifier.class)
                            .value();

            beanByName.put(name, bean);
        }
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
                    Object dependency = createBean(field.getType(), qualifierName);
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

    private void buildInterfacesMap(Set<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            if (!isValidBean(clazz)) {
                continue;
            }
            for (Class<?> iface : getAllInterfaces(clazz)) {
                interfaceImplementations
                        .computeIfAbsent(iface, k -> new ArrayList<>())
                        .add(clazz);
            }
        }
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

    private Constructor<?> getConstructor(Class<?> clazz) throws NoSuchMethodException {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() > 0) {
                return constructor;
            }
        }
        return clazz.getDeclaredConstructor();
    }

    private boolean isValidBean(Class<?> bean) {
        return bean.isAnnotationPresent(Service.class)
                || bean.isAnnotationPresent(Repository.class)
                || bean.isAnnotationPresent(Source.class);
    }

    private Class<?> resolveType(Class<?> clazz, String qualifierName) {
        if (!clazz.isInterface()) {
            return clazz;
        }

        List<Class<?>> implementations = interfaceImplementations.get(clazz);

        if (implementations == null || implementations.isEmpty()) {
            throw new RuntimeException(
                    "No implementation found for " + clazz.getName()
            );
        }

        if (qualifierName != null) {
            return getClassByQualifier(implementations, clazz, qualifierName);
        }

        return getClassWithPrimaryAnnotation(implementations);
    }

    private Class<?> getClassByQualifier(List<Class<?>> classes, Class<?> ifaceForErrorMsg, String qualifierName) {
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Qualifier.class)
                    && qualifierName.equals(clazz.getAnnotation(Qualifier.class).value())) {
                return clazz;
            }
        }

        return null;
    }

    private Class<?> getClassWithPrimaryAnnotation(List<Class<?>> classes) {
        if (classes.size() == 1) return classes.get(0);
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Primary.class)) return clazz;
        }
        throw new RuntimeException(
                "Multiple implementations found for "
                        + " but none marked @Primary and no @Qualifier specified"
        );
    }

    public <T> T getBean(Class<T> clazz) {
        return getBean(clazz, null);
    }

    public <T> T getBean(Class<T> clazz, String qualifierName) {
        try {
            Class<?> targetClass = resolveType(clazz, qualifierName);
            Object bean = singletonBeans.get(targetClass);

            if (bean == null) {
                bean = createBean(clazz, qualifierName);
            }

            return clazz.cast(bean);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
