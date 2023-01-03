package com.cqsd.spring.core.db;

import com.cqsd.spring.core.model.BeanDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author caseycheng
 * @date 2023/1/2-15:34
 **/
abstract public class Context {
    private static final List<Class<?>> CONFIG_CLASS = new ArrayList<>();
    private static final Map<String, BeanDefinition> BEAN_DEFINITION_MAP = new ConcurrentHashMap<>();
    private  static final Properties APP_ENV = new Properties();
    private static final Map<String,Object> singletionObjects=new ConcurrentHashMap<>();

    public static List<Class<?>> configClass() {
        return CONFIG_CLASS;
    }

    public static Map<String, BeanDefinition> beanDefinitionMap() {
        return BEAN_DEFINITION_MAP;
    }

    public static Properties appEnv() {
        return APP_ENV;
    }
    public static Map<String,Object> singletionobjects(){
        return singletionObjects;
    }
}
