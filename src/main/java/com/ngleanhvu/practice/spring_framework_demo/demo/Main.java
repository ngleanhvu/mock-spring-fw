package com.ngleanhvu.practice.spring_framework_demo.demo;

import com.ngleanhvu.practice.spring_framework_demo.core.ApplicationContext;

import java.lang.reflect.InvocationTargetException;

public class Main {
    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException {
        ApplicationContext context =
                new ApplicationContext("com.ngleanhvu.practice.spring_framework_demo");

        IUserService userService =
                context.getBean(IUserService.class);

        userService.createUser();

        context.close();
    }
}
