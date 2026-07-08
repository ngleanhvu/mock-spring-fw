package com.ngleanhvu.practice.spring_framework_demo.app;

import com.ngleanhvu.practice.spring_framework_demo.core.Qualifier;
import com.ngleanhvu.practice.spring_framework_demo.core.Service;

@Service
public class UserService implements IUserService {

    @Qualifier(value = "mongo")
    private final Database database;

    public UserService(Database database) {
        this.database = database;
    }

    public void createUser() {
        database.save();
        System.out.println("create user data");
    }
}
