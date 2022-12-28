package com.cqsd.spring.core.annotation;

import java.lang.annotation.*;

/**
 * 属性自动注入
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.METHOD})
@Inherited
public @interface Autowrite {
    
    String value() default "";
}
