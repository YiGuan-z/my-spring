package com.cqsd.core.annotation;

import java.lang.annotation.*;

/**
 * 属性自动装配
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.METHOD})
@Inherited
public @interface Autowrite {
    
    String value() default "";
}
