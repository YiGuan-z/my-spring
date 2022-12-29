package com.cqsd.spring.core.model;

import com.cqsd.spring.core.face.model.DefaltBeanDefintion;

/**
 * 一个bean对象的元信息
 */
public class BeanDefinition implements DefaltBeanDefintion {
    //这个bean的类型
    private Class<?> type;
    //这个bean的作用域
    private String scope;
    //这个bean被分配到的名字/标识
    private String name;
    //默认不是懒加载
    private boolean laze=false;
    @Override
    public Class<?> getType() {
        return type;
    }
    
    @Override
    public void setType(Class<?> type) {
        this.type = type;
    }
    
    @Override
    public String getScope() {
        return scope;
    }
    
    @Override
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public boolean isLaze() {
        return laze;
    }
    
    @Override
    public void setLaze(boolean laze) {
        this.laze = laze;
    }
}
