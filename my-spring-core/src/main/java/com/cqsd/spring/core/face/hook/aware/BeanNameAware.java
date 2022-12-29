package com.cqsd.spring.core.face.hook.aware;

/**
 * BeanName回调函数,通知被管理的bean被分配到的beanName
 */
public interface BeanNameAware extends Aware {
	void setBeanName(String beanName);
}
