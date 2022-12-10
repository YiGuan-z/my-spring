package com.cqsd.spring.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
/**
 * 用来定义是单例还是多例
 */
public @interface Scope {
    BeanScope value() default BeanScope.singleton;
     enum BeanScope{
         singleton,
         prototypes
    }
}
