package com.cqsd.spring.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author caseycheng
 * @date 2022/12/26-19:51
 **/
public class Transform {
    private static final Map<Class<?>, Function<Object, Object>> transHandler = new HashMap<>();

    static {
        transHandler.put(int.class, source -> Integer.valueOf(String.valueOf(source)));
//        transHandler.put(Integer.class, source -> Integer.valueOf(String.valueOf(source)));
        transHandler.put(String.class, String::valueOf);
        transHandler.put(byte.class, source -> Byte.valueOf(String.valueOf(source)));
        transHandler.put(Byte.class, source -> Byte.valueOf(String.valueOf(source)));
    }

    /**
     * 将一个对象转化为{@param type}的类型
     *
     * @param o    需要被转化的对象
     * @param type 转化后的类型
     * @return 几个常见的类型
     */
    public static Object trans(Object o, Class<?> type) {
        final var function = transHandler.get(type);
        if (function == null) {
            throw new NullPointerException("没有找到与类型" + type.getSimpleName() + "对应的处理器");
        }
        return function.apply(o);
    }

    public static void putTransFunc(Class<?> type, Function<Object, Object> action) {
        Transform.transHandler.put(type, action);
    }
}
