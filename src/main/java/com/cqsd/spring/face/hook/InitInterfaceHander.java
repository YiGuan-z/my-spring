package com.cqsd.spring.face.hook;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.stream.Stream;

public class InitInterfaceHander implements InvocationHandler {
	//代理实现一个接口
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		final var methodName = method.getName();
		System.out.printf("当前方法是%s",methodName);
		switch (methodName){
			case "toString"->{
				return new StringJoiner(",","{","}").add("null").toString();
			}
		}
		
		return null;
	}
}
