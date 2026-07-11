package com.ngleanhvu.practice.spring_framework_demo.app;

import com.ngleanhvu.practice.spring_framework_demo.core.*;

@Scope(value = "singleton")
@Service
public class UserService implements IUserService {

    @Autowired
    @Qualifier("mySqlDatabase")
    private  Database database;

    public void createUser() {
        database.save();
        System.out.println("create user data");
    }

    @PostConstruct
    public void init() {
        System.out.println("UserService running");
    }

    @PreDestroy
    public void close() {
        System.out.println("UserService destroyed");
    }
}
