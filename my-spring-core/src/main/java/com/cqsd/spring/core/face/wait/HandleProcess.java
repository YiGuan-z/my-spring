package com.cqsd.spring.core.face.wait;

import com.cqsd.spring.core.face.BeanFactory;

import java.lang.annotation.Annotation;

/**
 * 实现了这个接口并且被容器管理后将会传递bean被分配到的名字，和这个bean里面的annotation，
 * 还有需要处理对象的class类型，
 * 返回值将会交给容器，容器今后就使用处理好的BeanFactory对象。
 * 需要做一个pub sub 模型，某个注解将会对应一个注解处理器
 * TODO 计划实现
 */
@FunctionalInterface
public interface HandleProcess<A extends Annotation> {
	BeanFactory annotationProcess(String beanName, A anno, Class<?> type);
}
