package com.cqsd.spring;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.StringJoiner;

public class ProxyTest {
	static InvocationHandler handler;
	
	@BeforeAll
	static void init() {
		handler = (proxy, method, args) -> {
			final var name = method.getName();
			System.out.printf("代理的方法:%s\n",name);
			//配合xml
			if (name.equals("say")) {
				System.out.println(Arrays.toString(args));
			}
			if (name.equals("toString")){
				return new StringJoiner(",","{","}").add("null").toString();
			}
			
			return null;
		};
	}
	
	@Test
	void testProxy() {
		Hello hello = (Hello) Proxy.newProxyInstance(
				Hello.class.getClassLoader(),
				new Class[]{Hello.class},
				handler
		);
		final var handler1 = Proxy.getInvocationHandler(hello);
		System.out.println(handler1);
		hello.say("bob");
		System.out.println(hello);
		hello.get("sfa");
	}
}

interface Hello {
	void say(String name);
	
	void get(String sa);
}
