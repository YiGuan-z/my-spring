package com.cqsd.spring.core.face.model;

public interface DefaltBeanDefintion extends Defintion {
	void setScope(String scope);
	String getScope();
	void setName(String name);
	String getName();
	void setType(Class<?> type);
	Class<?> getType();
	void setLaze(boolean laze);
	boolean isLaze();
}
