package com.ngleanhvu.practice.spring_framework_demo.core;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class PackageScanner {

    public Set<Class<?>> scan(String packageName) {
        try {
            Set<Class<?>> classes = new HashSet<>();

            String path = packageName.replace(".", "/");

            ClassLoader classLoader =
                    Thread.currentThread()
                            .getContextClassLoader();

            URL resource =
                    classLoader.getResource(path);

            if (resource == null) {
                throw new RuntimeException(
                        "Package not found: " + packageName
                );
            }

            File directory =
                    new File(resource.getFile());

            scanDirectory(
                    packageName,
                    directory,
                    classes
            );

            return classes;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void scanDirectory(
            String packageName,
            File directory,
            Set<Class<?>> classes
    ) throws ClassNotFoundException {

        File[] files = directory.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {

            if (file.isDirectory()) {
                scanDirectory(
                        packageName + "." + file.getName(),
                        file,
                        classes
                );
            }

            if (file.getName().endsWith(".class")) {

                String className =
                        file.getName()
                                .replace(".class", "");

                String fullClassName =
                        packageName + "." + className;

                Class<?> clazz =
                        Class.forName(fullClassName);

                classes.add(clazz);
            }
        }
    }
}