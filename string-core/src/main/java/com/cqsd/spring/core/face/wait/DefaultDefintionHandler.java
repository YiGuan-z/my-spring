package com.cqsd.spring.core.face.wait;

import com.cqsd.spring.core.face.core.BeanFactory;
import com.cqsd.spring.core.face.core.model.DefaltBeanDefintion;
import com.cqsd.spring.core.face.core.model.Defintion;

/**
 * Defintion处理器
 */
public interface DefaultDefintionHandler extends Handler, BeanFactory {
	<T extends DefaltBeanDefintion> T DefintionHandlerProcess(Defintion defintion);
}
