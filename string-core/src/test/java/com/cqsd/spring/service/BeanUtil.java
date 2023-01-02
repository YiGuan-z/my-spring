package com.cqsd.spring.service;

import com.cqsd.core.annotation.Component;
import com.cqsd.spring.core.face.core.Application;
import com.cqsd.spring.core.face.hook.aware.ApplicationAware;

/**
 * @author caseycheng
 * @date 2022/12/24-10:09
 **/
@Component
public class BeanUtil implements ApplicationAware {
	private static Application application;
	@Override
	public void setApplication(Application application) {
		BeanUtil.application=application;
		System.out.println("application被注入了");
	}
}
