package com.cqsd.spring.core.face.hook.aware;

import com.cqsd.spring.core.face.Application;

public interface ApplicationAware extends Aware{
	void setApplication(Application application);
}
