package com.cqsd.spring.core.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.METHOD})
@Inherited
//自动注入
public @interface Autowrite {
    
    String value() default "";
}
