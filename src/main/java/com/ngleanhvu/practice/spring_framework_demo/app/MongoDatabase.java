package com.ngleanhvu.practice.spring_framework_demo.app;

import com.ngleanhvu.practice.spring_framework_demo.core.Qualifier;
import com.ngleanhvu.practice.spring_framework_demo.core.Source;

@Source("Mongo")
@Qualifier("Mongo")
public class MongoDatabase implements Database {
    @Override
    public void save() {
        String databaseName = this.getClass().getAnnotation(Source.class).value();
        System.out.printf("Save to %s%n", databaseName);
    }
}
