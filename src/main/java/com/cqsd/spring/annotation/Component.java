package com.cqsd.spring.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
/**
 * config bean name
 */
public @interface Component {
    //用于命名bean
    String value() default "";
}
