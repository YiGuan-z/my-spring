package com.cqsd.core.annotation;

import java.lang.annotation.*;

/**
 * 用来标记bean是单例还是多例,没有被注释就是单例,注释后不写值也是单例,只有里面的值不是singleton的时候才会是多例bean
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface Scope {
    String value() default "singleton";
}
