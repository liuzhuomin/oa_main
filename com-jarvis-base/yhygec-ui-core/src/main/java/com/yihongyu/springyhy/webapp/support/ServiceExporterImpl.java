package com.yihongyu.springyhy.webapp.support;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.gemini.blueprint.service.exporter.support.DefaultInterfaceDetector;
import org.eclipse.gemini.blueprint.service.exporter.support.OsgiServiceFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;

import com.yihongyu.springyhy.webapp.ServiceExporter;

public class ServiceExporterImpl implements ServiceExporter, BeanFactoryPostProcessor {

	private static Logger logger = LoggerFactory.getLogger(ServiceExporterImpl.class);

	private Map<String, Class<?>[]> shouldExportBeans = new HashMap<String, Class<?>[]>();

	private Set<String> alreadyExportBeans = new HashSet<String>();

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		DefaultListableBeanFactory factory = (DefaultListableBeanFactory) beanFactory;
		for (String name : factory.getBeanDefinitionNames()) {
			BeanDefinition bd = beanFactory.getBeanDefinition(name);
			if (bd instanceof ScannedGenericBeanDefinition) {
				shouldExportBeans.put(name, null);
			}
			else if (bd.getBeanClassName().equals(OsgiServiceFactoryBean.class.getName())) {
				alreadyExportBeans.add((String) bd.getPropertyValues().getPropertyValue("targetBeanName").getValue());
			}
		}
		
		for (String beanName : alreadyExportBeans) {
			shouldExportBeans.remove(beanName);
		}
		
		for (String beanName : shouldExportBeans.keySet()) {
			BeanDefinition exportBeanDefinition = buildExportBean(beanName);
			String exportBeanName = OsgiServiceFactoryBean.class.getName() + "#" + beanName;
			factory.registerBeanDefinition(exportBeanName, exportBeanDefinition);
			logger.debug("Registered OSGi service for bean: " + beanName);
		}

		addBeanPostProcessor(factory);
	}

	private BeanDefinition buildExportBean(String name) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(OsgiServiceFactoryBean.class);
		builder.addPropertyValue("targetBeanName", name);
		builder.addPropertyValue("interfaceDetector", DefaultInterfaceDetector.INTERFACES);
		return builder.getBeanDefinition();
	}

	private void addBeanPostProcessor(DefaultListableBeanFactory factory) {
		factory.addBeanPostProcessor(new BeanPostProcessor() {
			@Override
			public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
				return bean;
			}
			@Override
			public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
				if (shouldExportBeans.containsKey(beanName)) {
					shouldExportBeans.put(beanName, bean.getClass().getInterfaces());
				}
				return bean;
			}
		});
	}

	@Override
	public Map<String, Class<?>[]> getExportServices() {
		return shouldExportBeans;
	}
}
