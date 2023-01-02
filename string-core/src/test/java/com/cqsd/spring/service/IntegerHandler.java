package com.cqsd.spring.service;

import com.cqsd.core.annotation.Component;
import com.cqsd.spring.core.face.wait.MappingObject;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author caseycheng
 * @date 2023/1/2-11:41
 **/
@Component
public class IntegerHandler implements MappingObject {
    @Override
    public Supplier<? extends Class<?>> declareClass() {
        return () -> Integer.class;
    }

    @Override
    public Function<Object, Object> transFunc() {
        return (source) -> Integer.valueOf(String.valueOf(source));
    }
}
