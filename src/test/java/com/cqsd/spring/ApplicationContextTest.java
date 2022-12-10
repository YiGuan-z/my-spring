package com.cqsd.spring;

import com.cqsd.spring.face.Application;
import com.cqsd.spring.service.AppConfig;
import com.cqsd.spring.service.UserService;

class ApplicationContextTest {
	public static void main(String[] args) {
		Application app = new ApplicationContext(AppConfig.class);
		UserService service = (UserService) app.getBean("userService");
		final var userSrvice = app.getBean("userService", UserService.class);
		service.test();
		userSrvice.test();
	}
}