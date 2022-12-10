package com.cqsd.spring;

import com.cqsd.spring.face.Application;
import com.cqsd.spring.service.AppConfig;
import com.cqsd.spring.service.UserInterface;

class ApplicationContextTest {
	public static void main(String[] args) {
		Application app = new ApplicationContext(AppConfig.class);
		UserInterface service = (UserInterface) app.getBean("userService");
		final var userSrvice = app.getBean("userService", UserInterface.class);
		service.test();
		userSrvice.test();
	}
}