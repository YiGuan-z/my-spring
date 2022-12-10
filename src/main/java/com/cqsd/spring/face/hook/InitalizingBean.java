package com.cqsd.spring.face.hook;

/**
 * 初始化一个bean的方法
 */
public interface InitalizingBean {
	void afterPropertiesSet();
}
