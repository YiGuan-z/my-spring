package com.cqsd.spring.core.model;

/**
 * 一个bean对象的元信息
 */
public class BeanDefinition {
    //这个bean的类型
    private Class<?> type;
    //这个bean的作用域
    private String scope;
    //这个bean被分配到的名字/标识
    private String name;

    public Class<?> getType() {
        return type;
    }

    public BeanDefinition setType(Class<?> type) {
        this.type = type;
        return this;
    }

    public String getScope() {
        return scope;
    }

    public BeanDefinition setScope(String scope) {
        this.scope = scope;
        return this;
    }
    
    public String getName() {
        return name;
    }
    
    public BeanDefinition setName(String name) {
        this.name = name;
        return this;
    }
}
