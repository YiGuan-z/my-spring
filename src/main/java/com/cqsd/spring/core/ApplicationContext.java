package com.cqsd.spring.core;


import com.cqsd.spring.core.annotation.*;
import com.cqsd.spring.core.face.Application;
import com.cqsd.spring.core.face.hook.BeanPostProcess;
import com.cqsd.spring.core.face.hook.InitalizingBean;
import com.cqsd.spring.core.face.hook.aware.ApplicationAware;
import com.cqsd.spring.core.face.hook.aware.BeanNameAware;
import com.cqsd.spring.core.model.BeanDefinition;
import com.cqsd.spring.core.util.*;
import sun.misc.Unsafe;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationContext implements Application {
	//配置类集合
	private final static List<Class<?>> configClass = new ArrayList<>();
	//启动应用程序的类
	private static Class<?> mainApplicationClass;
	//bean信息池
	private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
	//单例池
	private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>();
	//Bean处理器池
	private final static List<BeanPostProcess> beanPostProcesslist = new ArrayList<>();
	//日后可能会用到的东西
	private final static Unsafe unsafe = UnsafeUtil.getUnsafe();
	//类加载器
	private final static ClassLoader classLoader = ApplicationContext.class.getClassLoader();
	//环境配置
	private final static Properties appProperties = new Properties();
	
	/**
	 * 对配置类进行初始化操作，找到上面定义的包扫描路径扫描出Bean组件，优先创建出来
	 *
	 * @param mainClass 被ComponentScan注释的配置类
	 */
	
	public ApplicationContext(Class<?> mainClass) {
		ApplicationContext.setMainApplicationClass(mainClass);
		//使用字符流来保证中文不会乱码
		loadProperties(getMainApplicationClass());
		final var applicationAnno = AnnotationUtil.getAnnotation(getMainApplicationClass(), ApplicationBoot.class);
		if (applicationAnno != null) {
			//如果存在java配置集合就把集合找出来,添加到configList中
			addJavaConfigClass();
			//通过java配置来对class文件进行加载
			for (Class<?> config : ApplicationContext.configClass) {
				if (config.isAnnotationPresent(ComponentScans.class)) {
					ComponentScans scans = AnnotationUtil.getAnnotation(config, ComponentScans.class);
					for (ComponentScan scan : scans.value()) {
						init(scan.value());
					}
				} else {
					init(AnnotationUtil.getAnnotation(config, ComponentScan.class).value());
				}
			}
			return;
		}
		throw new RuntimeException("没有被ApplicationBoot注解标识为一个应用程序类");
	}
	
	/**
	 * 通过MainApplicationClass来获取JavaConfig
	 */
	private void addJavaConfigClass() {
		if (AnnotationUtil.isAnnotation(getMainApplicationClass(), ApplicationConfigs.class)) {
			final var configs = AnnotationUtil.getAnnotation(getMainApplicationClass(), ApplicationConfigs.class);
			final var configList = Arrays.stream(configs.value()).distinct().map(ApplicationConfig::value).toList();
			ApplicationContext.configClass.addAll(configList);
		}
		if (AnnotationUtil.isAnnotation(getMainApplicationClass(), ApplicationConfig.class)) {
			final var config = AnnotationUtil.getAnnotation(getMainApplicationClass(), ApplicationConfig.class).value();
			ApplicationContext.configClass.add(config);
		}
		if (ApplicationContext.configClass.size() < 1) {
			throw new RuntimeException("请指定配置类");
		}
	}
	
	/**
	 * 对配置文件初始化
	 * @param mainApplicationClass 主方法的class
	 */
	private static void loadProperties(Class<?> mainApplicationClass) {
		URL url;
		String fileName = Constant.DEFAULT_CONFIG_FILE;
		if (AnnotationUtil.isAnnotation(mainApplicationClass, ApplicationConfigFile.class)) {
			fileName = AnnotationUtil.getAnnotation(mainApplicationClass, ApplicationConfigFile.class).value();
		}
		url = classLoader.getResource(fileName);
		if (url != null) {
			try (final var resource = new FileReader(url.getFile())) {
				appProperties.load(resource);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	/**
	 * 将路径下的class解析成BeanDefinition对象
	 * @param path 路径
	 */
	private void init(String path) {
		//path第一个/路径就是定义的包名
		var packageName = path.split("\\.")[0];
		//将Java包名做成找到Java文件的路径名
		path = path.replace('.', '/');
		//通过类加载器来获取本类下面的Java文件资源
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
		//对检测到的bean对象进行预先处理
		for (String beanName : beanDefinitionMap.keySet()) {
			BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
			//将单例创建出来放入单例池中
			if (beanDefinition.getScope().equals(Constant.SINGLETON)) {
				Object bean = createBean(beanDefinition);
				singletonObjects.put(beanName, bean);
			}
			//如果是bean对象处理器就立即实例化并放入bean对象处理器列表中
			if (BeanPostProcess.class.isAssignableFrom(beanDefinition.getType())) {
				beanPostProcesslist.add((BeanPostProcess) getBean(beanName));
			}
			//如果是应用程序回调函数同样立即实例化
			if (ApplicationAware.class.isAssignableFrom(beanDefinition.getType())) {
				final var bean = getBean(beanName);
				((ApplicationAware) bean).setApplication(this);
			}
		}
	}
	
	public Object createBean(BeanDefinition definition) {
		Class<?> clazz = definition.getType();
		//检查是否有无参构造
		final Constructor<?> noArgs = ConstructorUtil.findNoArgsConstructor(clazz);
		Object instance;
		if (noArgs != null) {
			instance = createNoArgsConstructor(noArgs, definition.getName());
		} else {
			//这里是有参构造
			final Constructor<?> constructor = ConstructorUtil.findAllArgsConstructor(clazz);
			instance = createAllArgsConstructor(constructor, definition.getName());
		}
		
		return instance;
	}
	
	/**
	 * 用于对实例中被{@link Value}注释了的字段进行注入
	 * @param instance 需要被注入的实例对象
	 */
	private void initProperties(Object instance) {
		
		if (instance == null)
			throw new NullPointerException("实例初始化错误");
		try {
			//获取待注入的对象
			final var fieldList = AnnotationUtil.annotationFields(instance.getClass().getDeclaredFields(), Value.class);
			if (fieldList.size() != 0) {
				for (Field field : fieldList) {
					var express = field.getDeclaredAnnotation(Value.class).value();
					Object value;
					if (StringUtil.isExtra(express)) {
						final var path = StringUtil.subExpr(express);
						value = appProperties.get(path);
					} else {
						value = Transform.transObject(express, field.getType());
					}
					field.setAccessible(true);
					field.set(instance, value);
				}
			}
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	/**
	 * 全参构造器
	 * @param constructor 寻找出来的全参构造器
	 * @param beanName bean的Name
	 * @return 构造完毕的对象
	 */
	private Object createAllArgsConstructor(Constructor<?> constructor, String beanName) {
		Object instance;
		try {
			//获取构造器需要的参数列表
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
						return ret;
					})
					.map(this::getBean)
					.toArray();
			//实例化对象
			instance = constructor.newInstance(args);
			
			//回调,告诉实例它被分配到的beanName
			initAware(instance, beanName);
			//初始化前
			for (BeanPostProcess process : beanPostProcesslist) {
				instance = process.postProcessBeforeInitalizing(beanName, instance);
			}
			//Autowrite 依赖注入简易版
			initField(instance);
			//在构造器对象创建出来后进行属性注入
			initProperties(instance);
			//用户做的初始化 属性被设置完毕后
			initHook(instance);
			//初始化后
			for (BeanPostProcess process : beanPostProcesslist) {
				instance = process.afterProcessBeforeInitalizing(beanName, instance);
			}
			
		} catch (Exception e) {
			throw new NullPointerException(String.format("找不到或没有那个bean\r\t%s", e.getMessage()));
		}
		return instance;
	}
	
	/**
	 * 无参构造器
	 * @param constructor 无参构造器
	 * @param beanName beanName
	 * @return 构造完毕的对象
	 */
	private Object createNoArgsConstructor(Constructor<?> constructor, String beanName) {
		try {
			Object instance = constructor.newInstance();
			//回调,告诉实例它被分配到的beanName
			initAware(instance, beanName);
			//初始化前
			for (BeanPostProcess process : beanPostProcesslist) {
				instance = process.postProcessBeforeInitalizing(beanName, instance);
			}
			//Autowrite 依赖注入简易版
			initField(instance);
			//在构造器对象创建出来后进行属性注入
			initProperties(instance);
			//初始化 属性被设置完毕后
			initHook(instance);
			//初始化后
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
	
	/**
	 * 告诉实例它被分配到的beanName
	 * @param instance 实例对象
	 * @param beanName beanName
	 */
	private void initAware(Object instance, String beanName) {
		//回调
		if (instance instanceof BeanNameAware ins) {
			ins.setBeanName(beanName);
		}
	}
	
	/**
	 * 对被{@link Autowrite} 注释了的属性进行对象注入
	 * @param instance 需要被注入属性的实例对象
	 * @throws IllegalAccessException 不满足装配条件的时候抛出异常
	 */
	private void initField(Object instance) throws IllegalAccessException {
		//找到被Autowrite注释了的字段
		final Class<?> clazz = instance.getClass();
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
			throw new RuntimeException(instance.getClass().getSimpleName()+"\r没有满足自动装配条件",e);
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
				instance = createBean( beanDefinition);
				singletonObjects.put(beanName, instance);
			}
			return instance;
		} else {
			//这里是多例
			return createBean( beanDefinition);
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
				instance = createBean( beanDefinition);
				singletonObjects.put(beanName, instance);
			}
			return (T) instance;
		} else {
			//多例
			return (T) createBean(beanDefinition);
		}
	}
	
	@SuppressWarnings({"unchecked"})
	@Override
	public <T> T getBean(Class<T> type) {
		final var definition = getBeanDefinition(type);
		Assert.requireNotNull(definition, new NullPointerException("没有那个bean"));
		final var scope = definition.getScope();
		final var beanName = definition.getName();
		if (scope.equals(Constant.SINGLETON)) {
			var instance = singletonObjects.get(definition.getName());
			if (instance == null) {
				instance = createBean(definition);
				singletonObjects.put(beanName, instance);
			}
			return (T) instance;
		} else {
			return (T) createBean(definition);
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
		Assert.requireNotNull(beanName);
		return beanDefinitionMap.get(beanName);
	}
	
	protected static Class<?> getMainApplicationClass() {
		return ApplicationContext.mainApplicationClass;
	}
	
	protected static void setMainApplicationClass(Class<?> mainApplicationClass) {
		ApplicationContext.mainApplicationClass = mainApplicationClass;
	}
	
	protected static List<Class<?>> getConfigClass() {
		return configClass;
	}
	
	protected static ClassLoader getClassLoader() {
		return classLoader;
	}
}
