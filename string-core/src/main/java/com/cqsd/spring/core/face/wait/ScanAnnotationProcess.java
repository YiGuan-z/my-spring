package com.cqsd.spring.core.face.wait;

import com.cqsd.spring.core.face.core.model.DefaltBeanDefintion;

import java.lang.annotation.Annotation;
@FunctionalInterface
public interface ScanAnnotationProcess<A extends Annotation> extends Handler {
    DefaltBeanDefintion annotationProcess(Class<?> type);
}
