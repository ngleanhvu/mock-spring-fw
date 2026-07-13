package com.ngleanhvu.practice.spring_framework_demo.demo;

import com.ngleanhvu.practice.spring_framework_demo.core.annotation.Repository;
import com.ngleanhvu.practice.spring_framework_demo.core.annotation.Source;

@Repository
@Source("Mongo")
public class MongoDatabase implements Database {
    @Override
    public void save() {
        String databaseName = this.getClass().getAnnotation(Source.class).value();
        System.out.printf("Save to %s%n", databaseName);
    }
}
