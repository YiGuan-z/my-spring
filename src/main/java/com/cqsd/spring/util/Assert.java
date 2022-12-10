package com.cqsd.spring.util;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author caseycheng
 * @date 2022/11/13-09:28
 **/
abstract public class Assert {
	public static void requireNotNull(Object obj, Throwable throwable) {
		if (obj == null) throw new RuntimeException(throwable);
	}
	
	public static void requireNotNull(Object obj, String message) {
		if (obj == null) throw new RuntimeException(message);
	}
	
	public static void requireNotNull(Object obj) {
		if (obj == null) throw new RuntimeException();
	}
	
	/**
	 * 这里的方法运算为真就报错
	 * @param t
	 * @param express
	 * @param message
	 * @param <T>
	 */
	public static <T>void assertFalse(T t,Predicate<T> express, String message) {
		if (express.test(t)) throw new RuntimeException(message);
	}
	
	/**
	 * 这里的方法运算为真就不报错
	 * @param t
	 * @param express
	 * @param message
	 * @param <T>
	 */
	public static <T>void assertTrue(T t,Predicate<T> express, String message) {
		if (!express.test(t)) throw new RuntimeException(message);
	}
	
	public static void requireNotNullMap(Map<?, ?> params) {
		Assert.requireNotNull(params);
		Assert.assertFalse(params,(param)->param.size() == 0, "该map为空");
	}
	
	public static void requireNotNullList(List<?> list) {
		Assert.requireNotNull(list);
		Assert.assertFalse(list,(t)->t.size() == 0, "该list为空");
	}
	
	public static <T> void requireInstance(Object o, Class<T> clazz) {
		requireInstance(o, clazz, String.format("类型%s\t和类型%s不同", o.getClass(), clazz));
	}
	
	public static <T> void requireInstance(Object o, Class<T> clazz, String message) {
		if (!o.getClass().isAssignableFrom(clazz)) throw new RuntimeException(message);
	}
	
}
