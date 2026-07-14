package com.ngleanhvu.practice.spring_framework_demo.demo;

import com.ngleanhvu.practice.spring_framework_demo.core.annotation.*;

@Service
public class UserService implements IUserService {

    @Autowired
    @Qualifier("mongoDatabase")
    private  Database database;

    @Transactional
    @Override
    public void createUser() {
        database.save();
        System.out.println("creating user data");
    }

    @Transactional
    @Override
    public void createOrderThatFails(String orderId) {
        database.save();
        System.out.println("creating user data");
        throw new RuntimeException("Data base error - rollback");
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
