package com.cqsd.spring.core.util;

/**
 * @author caseycheng
 * @date 2022/12/26-19:51
 **/
public class Transform {
	/**
	 * 将一个对象转化为{@param type}的类型
	 * @param o 需要被转化的对象
	 * @param type 转化后的类型
	 * @return 几个常见的类型
	 */
	public static Object transObject(Object o, Class<?> type) {
		if (int.class.equals(type) || Integer.class.equals(type)) {
			return Integer.valueOf(String.valueOf(o));
		} else if (String.class.equals(type)) {
			return String.valueOf(o);
		} else if (byte.class.equals(type) || Byte.class.equals(type)) {
			return Byte.valueOf(String.valueOf(o));
		}
		throw new IllegalStateException("Unexpected value: " + type);
	}
}
