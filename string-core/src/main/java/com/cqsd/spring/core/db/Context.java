package com.cqsd.spring.core.db;

import com.cqsd.spring.core.ApplicationContext;
import com.cqsd.spring.core.face.wait.ScanAnnotationProcess;
import com.cqsd.spring.core.model.BeanDefinition;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * @author caseycheng
 * @date 2023/1/2-15:34
 **/
abstract public class Context {
    private static final List<Class<?>> CONFIG_CLASS = new ArrayList<>();
    private static final Map<String, BeanDefinition> BEAN_DEFINITION_MAP = new ConcurrentHashMap<>();
    private static final Properties APP_ENV = new Properties();
    private static final Map<String, Object> singletionObjects = new ConcurrentHashMap<>();

    /**
     * 注解处理器上下文对象
     */
    static class AnnotationProcessContext {
        static class AnnotationException extends RuntimeException {
            /**
             * Constructs a new runtime exception with {@code null} as its
             * detail message.  The cause is not initialized, and may subsequently be
             * initialized by a call to {@link #initCause}.
             */
            public AnnotationException() {
            }

            /**
             * Constructs a new runtime exception with the specified detail message.
             * The cause is not initialized, and may subsequently be initialized by a
             * call to {@link #initCause}.
             *
             * @param message the detail message. The detail message is saved for
             *                later retrieval by the {@link #getMessage()} method.
             */
            public AnnotationException(String message) {
                super(message);
            }

            /**
             * Constructs a new runtime exception with the specified detail message and
             * cause.  <p>Note that the detail message associated with
             * {@code cause} is <i>not</i> automatically incorporated in
             * this runtime exception's detail message.
             *
             * @param message the detail message (which is saved for later retrieval
             *                by the {@link #getMessage()} method).
             * @param cause   the cause (which is saved for later retrieval by the
             *                {@link #getCause()} method).  (A {@code null} value is
             *                permitted, and indicates that the cause is nonexistent or
             *                unknown.)
             * @since 1.4
             */
            public AnnotationException(String message, Throwable cause) {
                super(message, cause);
            }

            /**
             * Constructs a new runtime exception with the specified cause and a
             * detail message of {@code (cause==null ? null : cause.toString())}
             * (which typically contains the class and detail message of
             * {@code cause}).  This constructor is useful for runtime exceptions
             * that are little more than wrappers for other throwables.
             *
             * @param cause the cause (which is saved for later retrieval by the
             *              {@link #getCause()} method).  (A {@code null} value is
             *              permitted, and indicates that the cause is nonexistent or
             *              unknown.)
             * @since 1.4
             */
            public AnnotationException(Throwable cause) {
                super(cause);
            }

            /**
             * Constructs a new runtime exception with the specified detail
             * message, cause, suppression enabled or disabled, and writable
             * stack trace enabled or disabled.
             *
             * @param message            the detail message.
             * @param cause              the cause.  (A {@code null} value is permitted,
             *                           and indicates that the cause is nonexistent or unknown.)
             * @param enableSuppression  whether or not suppression is enabled
             *                           or disabled
             * @param writableStackTrace whether or not the stack trace should
             *                           be writable
             * @since 1.7
             */
            public AnnotationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
                super(message, cause, enableSuppression, writableStackTrace);
            }
        }

        private static final Map<Class<? extends Annotation>, ScanAnnotationProcess<?>> annotationScanProcess = new ConcurrentHashMap<>();

        /**
         * 添加一个注解处理器
         *
         * @param annotationType        注解类型
         * @param scanAnnotationProcess 与该注解对应的注解处理器
         * @param <A>                   注解类型
         */
        public static <A extends Annotation> void addScanAnnotationProcessHandler(Class<A> annotationType, ScanAnnotationProcess<A> scanAnnotationProcess) {
            if (annotationScanProcess.containsKey(annotationType))
                throw new AnnotationException("不能重复添加一个注解处理器,必须先移除原有的注解处理器再添加新的");
            annotationScanProcess.put(annotationType, scanAnnotationProcess);
        }

        /**
         * 通过注解的类型来获取一个注解处理器
         *
         * @param annotationType 注解类型
         * @param <A>            注解类型
         * @return 注解处理器
         */
        @SuppressWarnings("unchecked")
        public static <A extends Annotation> ScanAnnotationProcess<A> getAnnotationProcessHandler(Class<? extends Annotation> annotationType) {
            return (ScanAnnotationProcess<A>) annotationScanProcess.get(annotationType);
        }

        /**
         * 移除一个注解处理器
         *
         * @param type 注解类型
         * @param <A>  注解类型
         */
        public static <A extends Annotation> void removeAnnotationProcessHandler(Class<A> type) {
            if (!annotationScanProcess.containsKey(type)) throw new AnnotationException("不能移除不存在的注解处理器");
            annotationScanProcess.remove(type);
        }

        /**
         * 遍历整个注解处理器池
         *
         * @param action 动作
         */
        public static void forEach(BiConsumer<Class<?>, ScanAnnotationProcess<?>> action) {
            if (action == null) throw new NullPointerException();
            final var set = annotationScanProcess.entrySet();
            for (Map.Entry<Class<? extends Annotation>, ScanAnnotationProcess<?>> entry : set) {
                action.accept(entry.getKey(), entry.getValue());
            }
        }
    }

    public static List<Class<?>> configClass() {
        return CONFIG_CLASS;
    }

    public static Map<String, BeanDefinition> beanDefinitionMap() {
        return BEAN_DEFINITION_MAP;
    }

    public static Properties appEnv() {
        return APP_ENV;
    }

    public static Map<String, Object> singletionobjects() {
        return singletionObjects;
    }
}
