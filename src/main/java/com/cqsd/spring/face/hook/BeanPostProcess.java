package com.cqsd.spring.face.hook;

/**
 * bean 属性赋值前后处理器
 */
public interface BeanPostProcess {
	Object postProcessBeforeInitalizing(String beanName,Object bean);
	Object afterProcessBeforeInitalizing(String beanName,Object bean);
}
