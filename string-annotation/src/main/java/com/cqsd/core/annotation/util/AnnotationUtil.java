package com.cqsd.core.annotation.util;

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
	/**
	 * 从给定字段中获取被某个注解表示的字段并返回
	 * @param fields 字段数组
	 * @param annotation 注解
	 * @return 被注解标记过的字段
	 */
	public static List<Field> annotationFields(Field[] fields, Class<? extends Annotation> annotation) {
		return Arrays.stream(fields).filter(field -> field.isAnnotationPresent(annotation))
				.collect(Collectors.toList());
	}
	
	/**
	 * 从给定方法中获取被注解的某个方法
	 * @param methods 方法数组
	 * @param annotation 注解
	 * @return 被注解标记了的方法
	 */
	public static List<Method> annotationMethods(Method[] methods, Class<? extends Annotation> annotation) {
		return Arrays.stream(methods).filter(method -> method.isAnnotationPresent(annotation))
				.collect(Collectors.toList());
	}
	
	/**
	 * 从类中获取某个注解
	 * @param type 类
	 * @param annotation 期望获取的annotation
	 * @return annotation 或者 null
	 * @param <T> annotation
	 */
	public static <T extends Annotation> T getAnnotation(Class<?> type, Class<T> annotation) {
		return type.getAnnotation(annotation);
	}
	
	/**
	 * 判断一个类是否有被某个注解标记
	 * @param type 类
	 * @param annotation 注解
	 * @return true or false
	 */
	public static boolean isAnnotation(Class<?> type, Class<? extends Annotation> annotation) {
		return type.isAnnotationPresent(annotation);
	}
}
