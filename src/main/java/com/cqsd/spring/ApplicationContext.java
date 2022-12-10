package com.cqsd.spring;


import com.cqsd.spring.annotation.*;
import com.cqsd.spring.face.Application;
import com.cqsd.spring.face.hook.BeanNameAware;
import com.cqsd.spring.face.hook.InitalizingBean;
import com.cqsd.spring.util.*;
import sun.misc.Unsafe;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ApplicationContext implements Application {
	//配置类
	private final Class<?> configClass;
	private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
	private final Map<String, List<BeanDefinition>> beanDefinitionCache = new ConcurrentHashMap<>();
	//单例池
	private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>();
	private final static Unsafe unsafe = UnsafeUtil.getUnsafe();
	
	/**
	 * 对配置类进行初始化操作，找到上面定义的包扫描路径扫描出Bean组件，优先创建出来
	 *
	 * @param configClass
	 */
	
	public ApplicationContext(Class<?> configClass) {
		this.configClass = configClass;
		if (this.configClass.isAnnotationPresent(ComponentScans.class)) {
			ComponentScans scans = this.configClass.getAnnotation(ComponentScans.class);
			for (ComponentScan scan : scans.value()) {
				init(scan.value());
			}
		} else {
			init(this.configClass.getAnnotation(ComponentScan.class).value());
		}
		
		
	}
	
	private void init(String path) {
		//path第一个/路径就是定义的包名
		var packageName = path.split("\\.")[0];
		//将Java包名做成找到Java文件的路径名
		path = path.replace('.', '/');
		//通过类加载器来获取本类下面的Java文件资源
		ClassLoader classLoader = ApplicationContext.class.getClassLoader();
		URL resource = classLoader.getResource(path);
		File file = new File(Objects.requireNonNull(resource).getFile());
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : Objects.requireNonNull(files)) {
				String fileName = f.getAbsolutePath();
				//定位包路径
				fileName = fileName.substring(fileName.indexOf(packageName))
						.replace('/', '.');
				//查找class文件
				if (fileName.endsWith(".class")) {
					try {
						//获取类路径
						String classname = fileName.substring(0, fileName.indexOf(".class"));
						Class<?> clazz = classLoader.loadClass(classname);
						//如果被Component注释了
						if (clazz.isAnnotationPresent(Component.class)) {
							final var beanDefinitionBuilder = Builder.builder(BeanDefinition::new);
							final var component = clazz.getAnnotation(Component.class);
							var beanName = component.value();
							//设置bean name
							String beanScope = Scope.BeanScope.singleton.name();
							if ("".equals(beanName)) {
//								beanName = Introspector.decapitalize(clazz.getSimpleName());
								beanName = StringUtil.toLowerCase(clazz.getSimpleName());
							}
							//设置作用域
							if (clazz.isAnnotationPresent(Scope.class)) {
								final var scope = clazz.getAnnotation(Scope.class);
								beanScope = scope.value().toString();
							}
							beanDefinitionBuilder
									.with(BeanDefinition::setType, clazz)
									.with(BeanDefinition::setName, beanName)
									.with(BeanDefinition::setScope, beanScope);
							beanDefinitionMap.put(beanName, beanDefinitionBuilder.build());
						}
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		for (String beanName : beanDefinitionMap.keySet()) {
			BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
			if (beanDefinition.getScope().equals(Scope.BeanScope.singleton.name())) {
				Object bean = createBean(beanName, beanDefinition);
				singletonObjects.put(beanName, bean);
			}
		}
	}
	
	public Object createBean(String beanName, BeanDefinition definition) {
		Class<?> clazz = definition.getType();
		try {
			Object instance = unsafe.allocateInstance(clazz);
			//Autowrite 依赖注入简易版
			initField(clazz, instance);
			//告诉bean的名字
			if (instance instanceof BeanNameAware ins) {
				ins.setBeanName(beanName);
			}
			//初始化操作，因为我这个不走构造函数，初始化操作在这个接口里
			if (instance instanceof InitalizingBean initalizingBean) {
				initalizingBean.init();
			}
			
			return instance;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void initField(Class<?> clazz, Object instance) throws IllegalAccessException {
		Field[] fields = clazz.getDeclaredFields();
		//找到被Autowrite注释了的字段
		for (Field field : fields) {
			if (field.isAnnotationPresent(Autowrite.class)) {
				field.setAccessible(true);
				field.set(instance, getBean(field.getName()));
			}
		}
		final var methods = clazz.getMethods();
		try {
			for (Method method : methods) {
				if (method.isAnnotationPresent(Autowrite.class)) {
					var name = method.getName();
					name = StringUtil.removeGetOrSet(name);
					name = StringUtil.toLowerCase(name);
					method.invoke(instance, getBean(name));
				}
			}
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	//	}
	@Override
	public Object getBean(String beanName) {
		//从bean信息池寻找这个bean
		BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
		if (beanDefinition == null) {
			throw new NullPointerException();
		} else {
			//获取这个bean的描述，是单例还是多例
			String scope = beanDefinition.getScope();
			if (scope.equals(Scope.BeanScope.singleton.name())) {
				Object bean = singletonObjects.get(beanName);
				if (bean == null) {
					Object o = createBean(beanName, beanDefinition);
					singletonObjects.put(beanName, o);
					return o;
				}
				return bean;
			} else {
				//这里是多例
				return createBean(beanName, beanDefinition);
			}
		}
	}
	
	@SuppressWarnings({"unchecked"})
	@Override
	public <T> T getBean(String beanName, Class<T> type) {
		final var beanDefinition = beanDefinitionMap.get(beanName);
		Assert.requireNotNull(beanDefinition, new NullPointerException("没有那个bean"));
		final var scope = beanDefinition.getScope();
		if (scope.equals(Scope.BeanScope.singleton.name())) {
			final var instance = (T) singletonObjects.get(beanName);
			if (instance == null) {
				final var bean = createBean(beanName, beanDefinition);
				singletonObjects.put(beanName, bean);
				return (T) bean;
			}
			return instance;
		} else {
			//多例
			return (T) createBean(beanName, beanDefinition);
		}
	}
	
	@Override
	public BeanDefinition getBeanDefinition(Class<?> type) {
		final var definition = beanDefinitionMap.values()
				.stream()
				.filter(beanDefinition -> beanDefinition.getType() == type)
				.findFirst();
		return definition.orElse(null);
	}
	
	@Override
	public BeanDefinition getBeanDefinition(String beanName) {
		return beanDefinitionMap.get(beanName);
	}
	
	
}
