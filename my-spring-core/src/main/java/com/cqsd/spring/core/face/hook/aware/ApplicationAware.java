package com.cqsd.spring.core.face.hook.aware;

import com.cqsd.spring.core.face.Application;

/**
 * 应用程序回调函数,实现了这个接口并交给容器管理后容器会调用这个方法传入一个application
 */
public interface ApplicationAware extends Aware{
	void setApplication(Application application);
}
