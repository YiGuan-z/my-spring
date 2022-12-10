package com.cqsd.spring.service;


import com.cqsd.spring.annotation.Component;

@Component
public class OrderService {
	{
		System.out.println("我是单例OrderService我初始化了");
	}
	public void test(){
		System.out.println("我是单例OrderService我被测试了");
	}
}
