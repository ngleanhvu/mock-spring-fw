package com.ngleanhvu.practice.spring_framework_demo.core;

import java.lang.annotation.Annotation;
import java.util.List;

public class Util {
    public static String getScopeOfBeanDefinition(Class<?> clazz) {

        if (!hasAnnotation(clazz, Scope.class)) {
            return Constant.SINGLETON;
        }

        String scope =
                clazz.getAnnotation(Scope.class)
                        .value();

        if (!Constant.PROTOTYPE.equalsIgnoreCase(scope)
                && !Constant.SINGLETON.equalsIgnoreCase(scope)) {

            throw new RuntimeException(
                    "Scope must be singleton or prototype"
            );
        }

        return scope;
    }

    public static boolean isSingleton(String scope) {
        return Constant.SINGLETON.equalsIgnoreCase(scope);
    }

    public static boolean hasAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
        return clazz.isAnnotationPresent(annotation);
    }


    public static String getBeanName(Class<?> clazz) {

        String className = clazz.getSimpleName();

        if (className.isEmpty()) {
            throw new RuntimeException(
                    "Cannot generate bean name"
            );
        }

        return Character.toLowerCase(className.charAt(0))
                + className.substring(1);
    }

    public static String extractQualifier(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof Qualifier) {
                return ((Qualifier) annotation).value();
            }
        }
        return null;
    }

    public static BeanDefinition getBeanByQualifier(
            List<BeanDefinition> definitions,
            String qualifierName
    ) {

        for (BeanDefinition definition : definitions) {

            if (definition.getBeanName()
                    .equals(qualifierName)) {

                return definition;
            }
        }

        return null;
    }

    public static boolean isValidBean(Class<?> bean) {

        return bean.isAnnotationPresent(Service.class)
                || bean.isAnnotationPresent(Repository.class)
                || bean.isAnnotationPresent(Source.class);
    }

}
