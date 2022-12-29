package com.cqsd.spring.core.model;

import com.cqsd.spring.core.annotation.Component;
import com.cqsd.spring.core.annotation.wait.Bean;
import com.cqsd.spring.core.face.BeanFactory;
import com.cqsd.spring.core.face.wait.HandleProcess;

/**
 * @author caseycheng
 * @date 2022/12/29-10:25
 **/
@Component
public class DefaultBeanDefinitionProcess implements HandleProcess<Bean> {
	@Override
	public BeanFactory annotationProcess(String beanName, Bean anno, Class<?> type) {
		return null;
	}
}
