package com.cqsd.spring.service;


import com.cqsd.spring.core.annotation.Component;
import com.cqsd.spring.core.annotation.Scope;
import com.cqsd.spring.core.annotation.Value;
import com.cqsd.spring.core.face.hook.BeanNameAware;
import com.cqsd.spring.core.face.hook.InitalizingBean;

@Component("userService")
@Scope("prototypes")
public class UserService implements BeanNameAware, InitalizingBean,UserInterface {
//	@Autowrite
	private final OrderService orderService;
	@Value("${hello}")
	private String beanName;
	public void test(){
		System.out.println(orderService);
		orderService.test();
	}
	
	public UserService(OrderService orderService) {
		this.orderService = orderService;
		System.out.println("我被my-spring初始化了");
	}
	
//	@Autowrite
//	public UserService setOrderService(OrderService orderService) {
//		this.orderService = orderService;
//		return this;
//	}
	
	@Override
	public void setBeanName(String beanName) {
		this.beanName=beanName;
	}
	
	@Override
	public void afterPropertiesSet() {
		System.out.println("用户在做初始化");
		System.out.println(this);
	}
}
