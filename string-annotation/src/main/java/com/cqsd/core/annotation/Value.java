package com.cqsd.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于对字段进行注入,当使用${xxx}表达式的时候,会从当前环境中获取这个变量并设置到字段中
 * 如果没有使用${}就会使用几个常见的类型将值设置到字段中
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Value {
	String value();
}
