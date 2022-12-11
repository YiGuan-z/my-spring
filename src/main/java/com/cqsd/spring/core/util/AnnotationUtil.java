package com.cqsd.spring.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author caseycheng
 * @date 2022/12/10-14:49
 **/
public abstract class AnnotationUtil {
	//从给定字段中获取被某个注解表示的字段并返回
	public static List<Field> annotationFields(Field[] fields, Class<? extends Annotation> annotation) {
		return Arrays.stream(fields).filter(field -> field.isAnnotationPresent(annotation))
				.collect(Collectors.toList());
	}
	
	//从给定方法中获取被注解的某个方法
	public static List<Method> annotationMethods(Method[] methods, Class<? extends Annotation> annotation) {
		return Arrays.stream(methods).filter(method -> method.isAnnotationPresent(annotation))
				.collect(Collectors.toList());
	}
}
