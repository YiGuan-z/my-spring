package com.cqsd.spring.core;

import com.cqsd.core.annotation.Autowrite;
import com.cqsd.core.annotation.Component;
import com.cqsd.core.annotation.Value;
import com.cqsd.core.annotation.util.AnnotationUtil;
import com.cqsd.spring.core.face.core.BeanFactory;
import com.cqsd.spring.core.face.hook.BeanPostProcess;
import com.cqsd.spring.core.face.hook.InitalizingBean;
import com.cqsd.spring.core.face.hook.aware.BeanNameAware;
import com.cqsd.spring.core.model.BeanDefinition;
import com.cqsd.spring.core.util.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author caseycheng
 * @date 2022/12/29-08:59
 **/
public class AppFactory implements BeanFactory {
    //bean信息池
    protected final static Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    //单例池
    protected final static Map<String, Object> singletonObjects = new ConcurrentHashMap<>();
    //Bean处理器池
    protected final static List<BeanPostProcess> beanPostProcesslist = new ArrayList<>();
    //配置文件环境
    protected final static Properties appProperties = new Properties();


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
                instance = createBean(beanDefinition);
                singletonObjects.put(beanName, instance);
            }
            return instance;
        } else {
            //这里是多例
            return createBean(beanDefinition);
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
                instance = createBean(beanDefinition);
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

    /**
     * 全参构造器
     *
     * @param constructor 寻找出来的全参构造器
     * @param beanName    bean的Name
     * @return 构造完毕的对象
     */
    public Object createAllArgsConstructor(Constructor<?> constructor, String beanName) {
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
            //对这个bean对象走一次初始化的生命周期
            instance = hookProcess(beanName, instance);

        } catch (Exception e) {
            throw new NullPointerException(String.format("找不到或没有那个bean\r\t%s", e.getMessage()));
        }
        return instance;
    }

    /**
     * 无参构造器
     *
     * @param constructor 无参构造器
     * @param beanName    beanName
     * @return 构造完毕的对象
     */
    public Object createNoArgsConstructor(Constructor<?> constructor, String beanName) {
        try {
            Object instance = constructor.newInstance();
            //对这个bean对象走一次初始化的生命周期
            instance = hookProcess(beanName, instance);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * bean实例创建过程中的生命周期处理
     *
     * @param beanName beanName
     * @param instance 需要进行的生命周期
     * @return 走完初始化生命周期后的对象
     * @throws IllegalAccessException 不满足装配的时候抛出的异常
     */
    public Object hookProcess(String beanName, Object instance) throws IllegalAccessException {
        //回调,告诉实例它被分配到的beanName
        if (instance instanceof BeanNameAware ins) {
            ins.setBeanName(beanName);
        }
        //初始化前
        for (BeanPostProcess process : beanPostProcesslist) {
            instance = process.postProcessBeforeInitalizing(beanName, instance);
        }
        //Autowrite 依赖注入简易版
        initField(instance);
        //在构造器对象创建出来后进行属性注入
        initProperties(instance);
        //初始化 属性被设置完毕后
        if (instance instanceof InitalizingBean initalizingBean) {
            initalizingBean.afterPropertiesSet();
        }
        //初始化后
        for (BeanPostProcess process : beanPostProcesslist) {
            instance = process.afterProcessBeforeInitalizing(beanName, instance);
        }
        return instance;
    }

    /**
     * 用于对实例中被{@link Value}注释了的字段进行注入
     *
     * @param instance 需要被注入的实例对象
     */
    public void initProperties(Object instance) {

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
                        value = Transform.trans(express, field.getType());
                    }
                    field.setAccessible(true);
                    field.set(instance, value);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }catch (NullPointerException e){
            throw new RuntimeException("对实例注入错误，原因是",e);
        }

    }

    /**
     * 对被{@link Autowrite} 注释了的属性进行对象注入
     *
     * @param instance 需要被注入属性的实例对象
     * @throws IllegalAccessException 不满足装配条件的时候抛出异常
     */
    public void initField(Object instance) throws IllegalAccessException {
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
            throw new RuntimeException(instance.getClass().getSimpleName() + "\r没有满足自动装配条件", e);
        }

    }

    public static Map<String, BeanDefinition> getBeanDefinitionMap() {
        return beanDefinitionMap;
    }

    public static Map<String, Object> getSingletonObjects() {
        return singletonObjects;
    }

    public static List<BeanPostProcess> getBeanPostProcesslist() {
        return beanPostProcesslist;
    }

    public static Properties getAppProperties() {
        return appProperties;
    }
}
