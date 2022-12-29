package com.cqsd.spring.core.annotation.wait;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识这是一个Java配置类，只有使用了这个注解的类才能被解析为一个配置类
 * TODO 计划实现
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Configuration {
}
