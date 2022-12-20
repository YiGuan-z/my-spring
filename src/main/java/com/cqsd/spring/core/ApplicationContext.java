package com.cqsd.spring.core;


import com.cqsd.spring.core.annotation.*;
import com.cqsd.spring.core.face.Application;
import com.cqsd.spring.core.face.hook.BeanNameAware;
import com.cqsd.spring.core.face.hook.BeanPostProcess;
import com.cqsd.spring.core.face.hook.InitalizingBean;
import com.cqsd.spring.core.util.*;
import sun.misc.Unsafe;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationContext implements Application {
	//配置类
	private final Class<?> configClass;
	private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
	//单例池
	private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>();
	private final List<BeanPostProcess> beanPostProcesslist = new ArrayList<>();
	private final static Unsafe unsafe = UnsafeUtil.getUnsafe();
	private final static ClassLoader classLoader = ApplicationContext.class.getClassLoader();
	private final static Properties appProperties = new Properties();
	
	/**
	 * 对配置类进行初始化操作，找到上面定义的包扫描路径扫描出Bean组件，优先创建出来
	 *
	 * @param configClass 被ComponentScan注释的配置类
	 */
	
	public ApplicationContext(Class<?> configClass) {
		//使用字符流来保证中文不会乱码
		initProprties();
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
	
	private static void initProprties() {
		final var url = classLoader.getResource("application.properties");
		if (url != null) {
			try (final var resource = new FileReader(url.getFile())) {
				appProperties.load(resource);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
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
				fileName = fileName.substring(fileName.indexOf(packageName)).replace('/', '.');
				//找到class文件并加载
				if (fileName.endsWith(".class")) {
					try {
						//获取类路径
						String classname = fileName.substring(0, fileName.indexOf(".class"));
						Class<?> clazz = classLoader.loadClass(classname);
						//如果是一个组件
						if (clazz.isAnnotationPresent(Component.class)) {
							
							final var beanDefinitionBuilder = Builder.builder(BeanDefinition::new);
							final var component = clazz.getAnnotation(Component.class);
							var beanName = component.value();
							//设置bean name 如果没有设置beanName就将类名的第一个字母小写
							if ("".equals(beanName)) {
								beanName = StringUtil.toLowerCase(clazz.getSimpleName());
							}
							//设置作用域 默认是单例
							String beanScope = Constant.SINGLETON;
							if (clazz.isAnnotationPresent(Scope.class)) {
								final var scope = clazz.getAnnotation(Scope.class);
								beanScope = scope.value();
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
			if (beanDefinition.getScope().equals(Constant.SINGLETON)) {
				Object bean = createBean(beanName, beanDefinition);
				singletonObjects.put(beanName, bean);
			}
			//如果是bean对象处理器
			if (BeanPostProcess.class.isAssignableFrom(beanDefinition.getType())) {
				beanPostProcesslist.add((BeanPostProcess) getBean(beanName));
			}
		}
	}
	
	public Object createBean(String beanName, BeanDefinition definition) {
		Class<?> clazz = definition.getType();
		//检查是否有无参构造
		final Constructor<?> noArgs = ConstructorUtil.findNoArgsConstructor(clazz);
		Object instance;
		if (noArgs != null) {
			instance = createNoArgsConstructor(clazz, noArgs, beanName);
		} else {
			//这里是有参构造
			final Constructor<?> constructor = ConstructorUtil.findAllArgsConstructor(clazz);
			instance = createAllArgsConstructor(constructor, beanName);
		}
		//属性注入
		initProperties(instance);
		
		
		return instance;
	}
	
	private void initProperties(Object instance) {
		//TODO 属性注入
	
		try {
			//获取待注入的对象
			final var fieldList = AnnotationUtil.annotationFields(instance.getClass().getDeclaredFields(), Value.class);
			if (fieldList.size() != 0) {
				for (Field field : fieldList) {
					var express = field.getDeclaredAnnotation(Value.class).value();
					final var path = StringUtil.subExpr(express);
					var value = appProperties.get(path);
					if (StringUtil.isExtra(express)) {
						//有额外的表达式需要拼接
						
					}
					field.setAccessible(true);
					field.set(instance, value);
				}
			}
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	private Object createAllArgsConstructor(Constructor<?> constructor, String beanName) {
		Object instance;
		try {
			final var types = constructor.getParameterTypes();
			final var args = Arrays.stream(types)
					.map(type -> {
						String ret;
						//都给容器管理了怎么可能没有Component.class注解 我智障了
//						if (AnnotationUtil.annotationClass(type, Component.class)) {
						//获取组件上的beanName
						final var value = AnnotationUtil.getAnnotation(type, Component.class).value();
						if (value.equals("")) {
							ret = StringUtil.toLowerCase(type.getSimpleName());
						} else {
							ret = value;
						}
//						} else {
//							ret = StringUtil.toLowerCase(type.getSimpleName());
//						}
						return ret;
					})
					.map(this::getBean)
					.toArray();
			instance = constructor.newInstance(args);
			for (BeanPostProcess process : beanPostProcesslist) {
				instance = process.afterProcessBeforeInitalizing(beanName, instance);
			}
		} catch (Exception e) {
			throw new NullPointerException(String.format("找不到或没有那个bean\r\t%s", e.getMessage()));
		}
		return instance;
	}
	
	private Object createNoArgsConstructor(Class<?> clazz, Constructor<?> constructor, String beanName) {
		try {
			Object instance = constructor.newInstance();
			//Autowrite 依赖注入简易版
			initField(clazz, instance);
			//回调
			initAware(instance, beanName);
			//前置处理
			for (BeanPostProcess process : beanPostProcesslist) {
				instance = process.postProcessBeforeInitalizing(beanName, instance);
			}
			//初始化
			initHook(instance);
			//后置处理
			for (BeanPostProcess process : beanPostProcesslist) {
				instance = process.afterProcessBeforeInitalizing(beanName, instance);
			}
			return instance;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void initHook(Object instance) {
		//初始化
		if (instance instanceof InitalizingBean initalizingBean) {
			initalizingBean.afterPropertiesSet();
		}
	}
	
	private void initAware(Object instance, String beanName) {
		//回调
		if (instance instanceof BeanNameAware ins) {
			ins.setBeanName(beanName);
		}
	}
	
	
	private void initField(Class<?> clazz, Object instance) throws IllegalAccessException {
		//找到被Autowrite注释了的字段
		final var fields = AnnotationUtil.annotationFields(clazz.getDeclaredFields(), Autowrite.class);
		for (Field field : fields) {
			field.setAccessible(true);
			field.set(instance, getBean(field.getName()));
		}
		final var methods = AnnotationUtil.annotationMethods(clazz.getMethods(), Autowrite.class);
		try {
			for (Method method : methods) {
				//基于set里面的参数类型进行自动装配
				final var types = method.getParameterTypes();
				for (Class<?> type : types) {
					final var bean = getBean(StringUtil.toLowerCase(type.getSimpleName()));
					method.invoke(instance, bean);
				}
			}
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	@Override
	public Object getBean(String beanName) {
		//从bean信息池寻找这个bean
		BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
		Assert.requireNotNull(beanDefinition, new NullPointerException("没有那个bean"));
		//获取这个bean的描述，是单例还是多例
		String scope = beanDefinition.getScope();
		if (scope.equals(Constant.SINGLETON)) {
			var instance = singletonObjects.get(beanName);
			if (instance == null) {
				instance = createBean(beanName, beanDefinition);
				singletonObjects.put(beanName, instance);
			}
			return instance;
		} else {
			//这里是多例
			return createBean(beanName, beanDefinition);
		}
	}
	
	
	@SuppressWarnings({"unchecked"})
	@Override
	public <T> T getBean(String beanName, Class<T> type) {
		final var beanDefinition = beanDefinitionMap.get(beanName);
		Assert.requireNotNull(beanDefinition, new NullPointerException("没有那个bean"));
		final var scope = beanDefinition.getScope();
		if (scope.equals(Constant.SINGLETON)) {
			var instance = singletonObjects.get(beanName);
			if (instance == null) {
				instance = createBean(beanName, beanDefinition);
				singletonObjects.put(beanName, instance);
			}
			return (T) instance;
		} else {
			//多例
			return (T) createBean(beanName, beanDefinition);
		}
	}
	
	@SuppressWarnings({"unchecked"})
	@Override
	public <T> T getBean(Class<T> type) {
//		final Map<? extends Class<?>, BeanDefinition> map = beanDefinitionMap.values().stream()
//				.collect(Collectors.toMap(BeanDefinition::getType, beanDefinition -> beanDefinition));
//		final var definition = map.get(type);
		final var definition = getBeanDefinition(type);
		Assert.requireNotNull(definition, new NullPointerException("没有那个bean"));
		final var scope = definition.getScope();
		final var beanName = definition.getName();
		if (scope.equals(Constant.SINGLETON)) {
			var instance = singletonObjects.get(definition.getName());
			if (instance == null) {
				instance = createBean(beanName, definition);
				singletonObjects.put(beanName, instance);
			}
			return (T) instance;
		} else {
			return (T) createBean(beanName, definition);
		}
	}
	
	@Override
	public BeanDefinition getBeanDefinition(Class<?> type) {
		final var definition = beanDefinitionMap
				.values()
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
