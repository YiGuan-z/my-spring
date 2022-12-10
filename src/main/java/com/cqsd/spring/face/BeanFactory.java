package com.cqsd.spring.face;

import com.cqsd.spring.util.BeanDefinition;

import java.util.List;

/**
 * @author caseycheng
 * @date 2022/12/9-19:52
 **/
public interface BeanFactory {
	Object getBean(String beanName);
	
	<T> T getBean(String beanName, Class<T> type);
	
	<T> List<T> getBean(Class<T> type);
	
	BeanDefinition getBeanDefinition(Class<?> type);
	
	BeanDefinition getBeanDefinition(String beanName);
	
	Object createBean(String beanName, BeanDefinition definition);
	
}
