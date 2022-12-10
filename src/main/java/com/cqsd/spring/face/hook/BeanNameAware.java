package com.cqsd.spring.face.hook;

/**
 * 通知bean，告诉它的名字
 */
public interface BeanNameAware {
	void setBeanName(String beanName);
}
