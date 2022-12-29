package com.cqsd.spring.core.face;

import com.cqsd.spring.core.model.BeanDefinition;

/**
 * @author caseycheng
 * @date 2022/12/9-19:52
 **/
public interface BeanFactory extends Factory{
	Object getBean(String beanName);
	
	<T> T getBean(String beanName, Class<T> type);
	
	<T> T getBean(Class<T> type);
	
	BeanDefinition getBeanDefinition(Class<?> type);
	
	BeanDefinition getBeanDefinition(String beanName);
	
	Object createBean(BeanDefinition definition);
	
}
