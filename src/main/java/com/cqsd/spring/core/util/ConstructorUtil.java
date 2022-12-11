package com.cqsd.spring.core.util;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Comparator;

/**
 * @author caseycheng
 * @date 2022/12/11-08:44
 **/
public abstract class ConstructorUtil {
	public static Constructor<?> findNoArgsConstructor(Class<?> clazz) {
		final var constructors = clazz.getDeclaredConstructors();
		final var optional = Arrays.stream(constructors)
				.filter(constructor -> constructor.getParameterCount() == 0)
				.findAny();
		if (optional.isEmpty()) {
			return null;
		}
		return optional.get();
	}
	public static Constructor<?> findAllArgsConstructor(Class<?> clazz){
		final var constructors = clazz.getDeclaredConstructors();
		final var constructor = Arrays.stream(constructors)
				.max(Comparator.comparing(Constructor::getParameterCount));
		if (constructor.isEmpty()){
			return null;
		}
		return constructor.get();
	}
	
}
