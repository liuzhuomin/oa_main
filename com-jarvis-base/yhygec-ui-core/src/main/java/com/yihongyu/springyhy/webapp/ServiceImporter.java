package com.yihongyu.springyhy.webapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceProxyFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.util.Assert;

public class ServiceImporter implements InitializingBean, BeanFactoryPostProcessor {

	private static Logger logger = LoggerFactory.getLogger(ServiceImporter.class);

	private List<ServiceExporter> serviceExporters = new ArrayList<ServiceExporter>();

	public void setServiceExporters(List<ServiceExporter> serviceExporters) {
		this.serviceExporters = serviceExporters;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notEmpty(serviceExporters, "ServiceExporter未导入");
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		DefaultListableBeanFactory factory = (DefaultListableBeanFactory) beanFactory;
		Map<String, Class<?>[]> exportServices = new HashMap<String, Class<?>[]>();
		for (ServiceExporter serviceExporter : serviceExporters) {
			exportServices.putAll(serviceExporter.getExportServices());
		}

		for (String beanName : exportServices.keySet()) {
			Class<?>[] interfaces = exportServices.get(beanName);
			if (interfaces != null && interfaces.length > 0) {
				BeanDefinition importBeanDefinition = buildImportBean(beanName, exportServices.get(beanName));
				factory.registerBeanDefinition(beanName, importBeanDefinition);
				logger.debug("Import OSGi service for bean: " + beanName);
			}
		}
	}

	private BeanDefinition buildImportBean(String name, Class<?>[] interfaces) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(OsgiServiceProxyFactoryBean.class);
		builder.addPropertyValue("beanName", name);
		builder.addPropertyValue("interfaces", interfaces);
		return builder.getBeanDefinition();
	}
}
