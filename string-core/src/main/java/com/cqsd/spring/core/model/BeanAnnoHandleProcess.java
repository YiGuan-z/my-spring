package com.cqsd.spring.core.model;


import com.cqsd.core.annotation.Component;
import com.cqsd.core.annotation.wait.Bean;
import com.cqsd.spring.core.face.core.BeanFactory;
import com.cqsd.spring.core.face.wait.AnnoHandleProcess;

import java.lang.annotation.Annotation;

/**
 * @author caseycheng
 * @date 2022/12/29-10:25
 **/
@Component
public class BeanAnnoHandleProcess implements AnnoHandleProcess<Bean> {
	/**
	 * 返回一个与之对应的Bean工厂
	 * @param anno 被扫描到的注解
	 * @param type 被注解注释的类型
	 * @return 一个对应的Bean工厂
	 */
	@Override
	public BeanFactory annotationProcess(Bean anno, Class<?> type) {
		return new BeanAnnotationHandle(anno,type);
	}

	static class BeanAnnotationHandle implements BeanFactory{

		public <T extends Annotation> BeanAnnotationHandle(T annoType,Class<?> type) {
		}

		@Override
		public Object getBean(String beanName) {
			return null;
		}

		@Override
		public <T> T getBean(String beanName, Class<T> type) {
			return null;
		}

		@Override
		public <T> T getBean(Class<T> type) {
			return null;
		}

		@Override
		public BeanDefinition getBeanDefinition(Class<?> type) {
			return null;
		}

		@Override
		public BeanDefinition getBeanDefinition(String beanName) {
			return null;
		}

		@Override
		public Object createBean(BeanDefinition definition) {
			return null;
		}
	}
}
