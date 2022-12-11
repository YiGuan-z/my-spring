package com.cqsd.spring.core.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
/**
 * 用来定义是单例还是多例
 */
public @interface Scope {
    String value() default "";
}
