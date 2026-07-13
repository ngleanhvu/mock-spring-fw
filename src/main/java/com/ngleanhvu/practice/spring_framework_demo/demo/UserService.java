package com.ngleanhvu.practice.spring_framework_demo.demo;

import com.ngleanhvu.practice.spring_framework_demo.core.annotation.Autowired;
import com.ngleanhvu.practice.spring_framework_demo.core.annotation.Qualifier;
import com.ngleanhvu.practice.spring_framework_demo.core.annotation.Service;

@Service
public class UserService implements IUserService {

    @Autowired
    @Qualifier("mongoDatabase")
    private  Database database;

    public void createUser() {
        database.save();
        System.out.println("create user data");
    }

    public void init() {
        System.out.println("UserService running");
    }

    public void close() {
        System.out.println("UserService destroyed");
    }
}
