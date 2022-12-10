package com.cqsd.spring.service;


import com.cqsd.spring.annotation.Autowrite;
import com.cqsd.spring.annotation.Component;
import com.cqsd.spring.annotation.Scope;
import com.cqsd.spring.face.hook.BeanNameAware;
import com.cqsd.spring.face.hook.InitalizingBean;

@Component("userService")
@Scope(
		Scope.BeanScope.prototypes
)
public class UserService implements BeanNameAware, InitalizingBean {
//	@Autowrite
	private OrderService orderService;
	private String beanName;
	public void test(){
		System.out.println(orderService);
		orderService.test();
	}
	@Autowrite
	public UserService setOrderService(OrderService orderService) {
		this.orderService = orderService;
		return this;
	}
	
	@Override
	public void setBeanName(String beanName) {
		this.beanName=beanName;
	}
	
	@Override
	public void init() {
		System.out.println("用户在做初始化");
	}
}
