package com.ngleanhvu.practice.spring_framework_demo;

import com.ngleanhvu.practice.spring_framework_demo.app.IUserService;
import com.ngleanhvu.practice.spring_framework_demo.core.ApplicationContext;

public class Main {
    public static void main(String[] args) {
        ApplicationContext context =
                new ApplicationContext("com.ngleanhvu.practice.spring_framework_demo");

        IUserService userService =
                context.getBean(IUserService.class);

        userService.createUser();
    }
}
