package com.cqsd.spring.core.face.wait;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 这个类用来对对象进行映射，将一个对象转化为另一个对象。
 * 泛型参数标识适用于什么类型
 */
public interface MappingObject {
    Supplier<? extends Class<?>> declareClass();
    Function<Object,Object> transFunc();
}
