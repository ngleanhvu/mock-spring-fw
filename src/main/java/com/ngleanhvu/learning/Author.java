package com.ngleanhvu.learning;

import java.lang.annotation.*;

@Documented
@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Author {
    String name();
    String email();
}
