package com.cqsd.spring.service;

import com.cqsd.spring.annotation.Component;
import com.cqsd.spring.face.hook.BeanPostProcess;

import java.lang.reflect.Proxy;

/**
 * @author caseycheng
 * 可以在这里创建代理对象并返回
 * @date 2022/12/10-15:43
 **/
@Component
public class BeanProcess implements BeanPostProcess {
	@Override
	public Object postProcessBeforeInitalizing(String beanName, Object bean) {
		System.out.println(beanName+"初始化前");
		System.out.println();
		return bean;
	}
	
	@Override
	public Object afterProcessBeforeInitalizing(String beanName, Object bean) {
		if (beanName.equals("userService")){
			final var newProxyInstance = Proxy.newProxyInstance(
					BeanProcess.class.getClassLoader(),
					bean.getClass().getInterfaces(),
					(proxy, method, args) -> {
						System.out.println("小夫，我要进来了");
						return method.invoke(bean,args);
					}
			);
			return newProxyInstance;
		}
		return bean;
	}
}
