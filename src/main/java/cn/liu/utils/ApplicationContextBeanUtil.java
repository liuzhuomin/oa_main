package cn.liu.utils;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * <li>��ȡsprng�����ĵĹ�����,��Ҫע���bean
 * @author liuliuliu
 *
 */
@Component
public class ApplicationContextBeanUtil implements ApplicationContextAware {

	private static ApplicationContext applicationContext;

	/**
	 * ����awareע��application
	 * 
	 * @param applicationContext
	 * @throws BeansException
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException { // ע��application
		ApplicationContextBeanUtil.applicationContext = applicationContext;
	}

	private static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	/**
	 * } ͨ��name��ȡbean
	 * 
	 * @param name
	 * @return
	 */
	public static Object getBean(String name) {
		return getApplicationContext().getBean(name);
	}

	/**
	 * ͨ��class��ȡbean
	 * 
	 * @param clazz
	 * @param       <T>
	 * @return
	 */
	public static <T> T getBean(Class<T> clazz) {
		return getApplicationContext().getBean(clazz);
	}

	/**
	 * ͨ��name��class��ȡbean
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
	 * ����clazz���ͻ�ȡspring�����еĶ���
	 * 
	 * @param clazz
	 * @param       <T>
	 * @return
	 */
	public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
		return getApplicationContext().getBeansOfType(clazz);
	}

	/**
	 * ����ע����������л�ȡ����
	 * 
	 * @param clazz
	 * @param       <T>
	 * @return
	 */
	public static <T> Map<String, Object> getBeansOfAnnotation(Class<? extends Annotation> clazz) {
		return getApplicationContext().getBeansWithAnnotation(clazz);
	}
}
