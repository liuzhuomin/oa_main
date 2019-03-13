package cn.liu.utils;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * <li>获取sprng上下文的工具类,需要注册成bean
 * @author liuliuliu
 *
 */
@Component
public class ApplicationContextBeanUtil implements ApplicationContextAware {

	private static ApplicationContext applicationContext;

	/**
	 * 利用aware注入application
	 * 
	 * @param applicationContext
	 * @throws BeansException
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException { // 注入application
		ApplicationContextBeanUtil.applicationContext = applicationContext;
	}

	private static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	/**
	 * } 通过name获取bean
	 * 
	 * @param name
	 * @return
	 */
	public static Object getBean(String name) {
		return getApplicationContext().getBean(name);
	}

	/**
	 * 通过class获取bean
	 * 
	 * @param clazz
	 * @param       <T>
	 * @return
	 */
	public static <T> T getBean(Class<T> clazz) {
		return getApplicationContext().getBean(clazz);
	}

	/**
	 * 通过name和class获取bean
	 * 
	 * @param name
	 * @param clazz
	 * @param       <T>
	 * @return
	 */
	public static <T> T getBean(String name, Class<T> clazz) {
		return getApplicationContext().getBean(name, clazz);
	}

	/**
	 * 根据clazz类型获取spring容器中的对象
	 * 
	 * @param clazz
	 * @param       <T>
	 * @return
	 */
	public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
		return getApplicationContext().getBeansOfType(clazz);
	}

	/**
	 * 根据注解类从容器中获取对象
	 * 
	 * @param clazz
	 * @param       <T>
	 * @return
	 */
	public static <T> Map<String, Object> getBeansOfAnnotation(Class<? extends Annotation> clazz) {
		return getApplicationContext().getBeansWithAnnotation(clazz);
	}
}
