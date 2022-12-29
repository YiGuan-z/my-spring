package com.cqsd.ps.interfaces;

import java.util.concurrent.TimeUnit;

/**
 * 发布者对象
 */
public interface Publisher {
	
	//缓冲
	void setBuffer(int buffer);
	int getBuffer();
	//超时时间
	void setTimeOut(Long time, TimeUnit unit);
	long getTimeOut();
	//订阅者管道
	interface SFunction<T,R>{
		R apply(T t);
	}
}
