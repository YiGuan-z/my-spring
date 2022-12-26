package com.cqsd.spring.core.util;

/**
 * @author caseycheng
 * @date 2022/12/26-19:51
 **/
@SuppressWarnings("unchecked")
public class Transform {
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
