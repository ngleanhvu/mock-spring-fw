package com.ngleanhvu.practice.spring_framework_demo;

import com.ngleanhvu.practice.spring_framework_demo.app.IUserService;
import com.ngleanhvu.practice.spring_framework_demo.core.ApplicationContextV1;

import java.lang.reflect.InvocationTargetException;

public class Main {
    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException {
        ApplicationContextV1 context =
                new ApplicationContextV1("com.ngleanhvu.practice.spring_framework_demo");

        IUserService userService =
                context.getBean(IUserService.class);

        userService.createUser();

        context.close();
    }
}
