package com.cqsd.spring.core.annotation.wait;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识这是一个Bean对象，需要被封装到BeanDefintion里面,默认不是懒加载
 * TODO 计划实现
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Bean {
}
