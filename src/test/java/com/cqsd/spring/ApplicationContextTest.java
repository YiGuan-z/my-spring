package com.cqsd.spring;

import com.cqsd.spring.core.ApplicationContext;
import com.cqsd.spring.core.face.Application;
import com.cqsd.spring.service.AppConfig;
import com.cqsd.spring.service.UserInterface;
import org.junit.jupiter.api.Test;

class ApplicationContextTest {
	public static void main(String[] args) {
		Application app = new ApplicationContext(AppConfig.class);
		UserInterface service = (UserInterface) app.getBean("userService");
		final var userSrvice = app.getBean("userService", UserInterface.class);
		service.test();
		userSrvice.test();
	}
	@Test
	void testIoc(){
		final Application context = new ApplicationContext(AppConfig.class);
		final var bean = context.getBean("userService",UserInterface.class);
//		final var bean = context.getBean(UserInterface.class);
		bean.test();
	}
}