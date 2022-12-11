package com.cqsd.spring.core.face.hook;

/**
 * bean 属性赋值前后处理器
 * 对于构造器注入没有postProcessBeforeInitalizing，只能使用afterProcessBeforeInitalizing
 */
public interface BeanPostProcess {
	Object postProcessBeforeInitalizing(String beanName,Object bean);
	Object afterProcessBeforeInitalizing(String beanName,Object bean);
}
