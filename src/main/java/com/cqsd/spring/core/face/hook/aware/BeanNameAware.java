package com.cqsd.spring.core.face.hook.aware;

/**
 * 通知bean，告诉它的名字
 */
public interface BeanNameAware extends Aware {
	void setBeanName(String beanName);
}
