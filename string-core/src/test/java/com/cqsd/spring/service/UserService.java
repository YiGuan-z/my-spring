package com.cqsd.spring.service;



import com.cqsd.core.annotation.Component;
import com.cqsd.core.annotation.Scope;
import com.cqsd.core.annotation.Value;
import com.cqsd.spring.core.face.hook.InitalizingBean;
import com.cqsd.spring.core.face.hook.aware.BeanNameAware;

@Component("userService")
@Scope("prototypes")
public class UserService implements BeanNameAware, InitalizingBean, UserInterface {
	//	@Autowrite
	private final OrderService orderService;
	//在bean初始化完成后注入 createBean后面注入参数值
	private String beanName;
	@Value("${hello}")
	private String message;
	@Value("wo")
	private String ni;
	@Value("1")
	private Integer num;
	
	public void test() {
		System.out.println(orderService);
		orderService.test();
		System.out.println("beanName = " + beanName);
		System.out.println("message = " + message);
		System.out.println("ni = " + ni);
		System.out.println("num = " + num);
	}
	
	public UserService(OrderService orderService) {
		this.orderService = orderService;
		System.out.println("我是UserService我被初始化了");
	}

//	@Autowrite
//	public UserService setOrderService(OrderService orderService) {
//		this.orderService = orderService;
//		return this;
//	}
	
	@Override
	public void setBeanName(String beanName) {
		this.beanName = beanName;
		System.out.println(beanName);
	}
	
	@Override
	public void afterPropertiesSet() {
		System.out.println("用户在做初始化");
		System.out.println(this);
	}
}
