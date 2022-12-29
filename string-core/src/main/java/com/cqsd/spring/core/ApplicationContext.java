package com.cqsd.spring.core;


import com.cqsd.spring.core.annotation.*;
import com.cqsd.spring.core.face.core.Application;
import com.cqsd.spring.core.face.hook.BeanPostProcess;
import com.cqsd.spring.core.face.hook.aware.ApplicationAware;
import com.cqsd.spring.core.model.BeanDefinition;
import com.cqsd.spring.core.util.*;
import sun.misc.Unsafe;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ApplicationContext extends AppFactory implements Application  {
	//配置类集合
	private final static List<Class<?>> configClass = new ArrayList<>();
	//启动应用程序的类
	private static Class<?> mainApplicationClass;
	//日后可能会用到的东西
	private final static Unsafe unsafe = UnsafeUtil.getUnsafe();
	//类加载器
	private final static ClassLoader classLoader = ApplicationContext.class.getClassLoader();
	
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
			final var configList = getConfigClass();
			for (Class<?> config : configList) {
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
