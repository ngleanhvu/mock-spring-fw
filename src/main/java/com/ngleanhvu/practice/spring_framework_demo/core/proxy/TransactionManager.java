package com.ngleanhvu.practice.spring_framework_demo.core.proxy;

public class TransactionManager {

    public void begin() {
        System.out.println("Begin transaction");
    }

    public void commit() {
        System.out.println("Commit transaction");
    }

    public void rollback(Throwable cause) {
        System.out.println("Rollback transaction. Reason: " + cause.getMessage());
    }
}
