package com.cqsd.core.annotation;

import java.lang.annotation.*;

/**
 * 标识这是一个Bean,value标识这个bean自定义的名字
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface Component {
    String value() default "";
}
