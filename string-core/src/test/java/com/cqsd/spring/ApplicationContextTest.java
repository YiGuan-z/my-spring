package com.cqsd.spring;

import com.cqsd.core.annotation.ApplicationBoot;
import com.cqsd.core.annotation.ApplicationConfig;
import com.cqsd.core.annotation.ApplicationConfigFile;
import com.cqsd.spring.core.ApplicationContext;

import com.cqsd.spring.core.face.core.Application;
import com.cqsd.spring.service.AppConfig;
import com.cqsd.spring.service.UserInterface;
import org.junit.jupiter.api.Test;
//指定配置文件名字
@ApplicationConfigFile("application.properties")
//解析java配置
@ApplicationConfig(AppConfig.class)
//只有被ApplicationBoot标识了的类才能被启动
@ApplicationBoot
class ApplicationContextTest {
	@Test
	void testError() {
		try {
			Application app = new ApplicationContext(AppConfig.class);
			UserInterface service = (UserInterface) app.getBean("userService");
			final var userSrvice = app.getBean("userService", UserInterface.class);
			service.test();
			userSrvice.test();
		}catch (RuntimeException e){
			System.out.println("检测mainClass测试成功");
		}
		
	}
	@Test
	void testIoc(){
		final Application context = new ApplicationContext(ApplicationContextTest.class);
		final var bean =(UserInterface) context.getBean("userService",UserInterface.class);
		bean.test();
	}
}