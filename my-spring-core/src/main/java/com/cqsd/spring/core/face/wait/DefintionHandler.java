package com.cqsd.spring.core.face.wait;

import com.cqsd.spring.core.face.BeanFactory;
import com.cqsd.spring.core.face.model.DefaltBeanDefintion;
import com.cqsd.spring.core.face.model.Defintion;

/**
 * Defintion处理器
 */
public interface DefintionHandler extends Handler, BeanFactory {
	<T extends DefaltBeanDefintion> T DefintionHandlerProcess(Defintion defintion);
}
