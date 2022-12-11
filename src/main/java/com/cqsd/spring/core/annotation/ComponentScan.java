package com.cqsd.spring.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
/**
 * 配置包扫描
 */
public @interface ComponentScan {
    //这是扫描路径
    String value() default "";
}
