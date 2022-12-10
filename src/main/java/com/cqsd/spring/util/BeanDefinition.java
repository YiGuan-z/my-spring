package com.cqsd.spring.util;

public class BeanDefinition {
    private Class<?> type;
    private String scope;
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
