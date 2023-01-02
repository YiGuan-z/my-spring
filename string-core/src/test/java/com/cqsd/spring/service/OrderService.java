package com.cqsd.spring.service;


import com.cqsd.core.annotation.Component;
import com.cqsd.spring.core.face.hook.InitalizingBean;

@Component
public class OrderService implements InitalizingBean {
	{
		System.out.println("我是单例OrderService我初始化了");
	}
	
	@Override
	public void afterPropertiesSet() {
		System.out.println("我是orderService被initlaizingBean接口初始化了");
	}
	
	public void test(){
		System.out.println("我是单例OrderService我被测试了");
	}
}
