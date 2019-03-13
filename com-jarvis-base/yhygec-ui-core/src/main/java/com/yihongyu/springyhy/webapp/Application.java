package com.yihongyu.springyhy.webapp;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.eclipse.gemini.blueprint.util.OsgiFilterUtils;
import org.eclipse.gemini.blueprint.util.OsgiListenerUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.Assert;

import com.yihongyu.springyhy.user.entity.User;
import com.yihongyu.yhyweb.rbac.RbacService;

/**
 * Application Object.
 * <p/>
 * web 应用entry.
 */
public class Application implements DisposableBean, BundleContextAware {

	private static final String ENSURE_INJECT_MESSAGE = "确保%s已注入Application, 检查是否配置SpringyhyWebApplicationFilter";
	private static ThreadLocal<HttpServletRequest> requestHolder = new ThreadLocal<HttpServletRequest>();
	private static ThreadLocal<HttpServletResponse> responseHolder = new ThreadLocal<HttpServletResponse>();

	private static BundleContext bundleContext;
	private static Map<Class<?>, ServiceListener> serviceListeners = new HashMap<Class<?>, ServiceListener>();
	private static Map<Class<?>, ServiceReference<?>> serviceReferences = new HashMap<Class<?>, ServiceReference<?>>();
	private static RbacService rbacService;

	/**
	 * 获取指定类型的bean. 如果有多个Bean是此类型, 取第一个.
	 */
	public static <T> T getBean(Class<T> requiredType) {
		return getProxyBean(requiredType);
	}

	@SuppressWarnings("unchecked")
	private static <T> T getProxyBean(final Class<T> requiredType) {
		return (T) ProxyFactory.getProxy(new TargetSource() {
			@Override
			public Class<?> getTargetClass() {
				return requiredType;
			}
			@Override
			public boolean isStatic() {
				return false;
			}
			@Override
			public Object getTarget() throws Exception {
				return Application.doGetBean(requiredType);
			}
			@Override
			public void releaseTarget(Object target) throws Exception {
				// do nothing				
			}
		});
	}

	@SuppressWarnings("unchecked")
	private static <T> T doGetBean(final Class<T> requiredType) {
		if (!serviceListeners.containsKey(requiredType)) {
			addServiceListener(requiredType);
		}

		if (!serviceReferences.containsKey(requiredType)) {
			throw new IllegalStateException("未能装载服务: " + requiredType);
		}
		return (T) bundleContext.getService(serviceReferences.get(requiredType));
	}

	private static void addServiceListener(final Class<?> serviceClass) {
		ServiceListener listener = new ServiceListener() {
			@Override
			public void serviceChanged(ServiceEvent event) {
				switch(event.getType()) {
					case (ServiceEvent.REGISTERED):
					case (ServiceEvent.MODIFIED): {
						addServiceReference(serviceClass, event.getServiceReference());
						break;
					}
					case (ServiceEvent.UNREGISTERING): {
						removeServiceReference(serviceClass);
					}
				}
				
			}
		};
		serviceListeners.put(serviceClass, listener);
		OsgiListenerUtils.addSingleServiceListener(bundleContext, listener, OsgiFilterUtils.unifyFilter(serviceClass, null));
	}

	private static void removeServiceListener(Class<?> serviceClass) {
		ServiceListener listener = serviceListeners.get(serviceClass);
		if (listener != null) {
			OsgiListenerUtils.removeServiceListener(bundleContext, listener);
		}
	}

	private static void addServiceReference(Class<?> serviceClass, ServiceReference<?> reference) {
		serviceReferences.put(serviceClass, reference);
	}

	private static ServiceReference<?> removeServiceReference(Class<?> serviceClass) {
		ServiceReference<?> reference = serviceReferences.remove(serviceClass);
		if (reference != null) {
			bundleContext.ungetService(reference);
		}
		return reference;
	}

	/**
	 * 获取当前登录用户.
	 * <p/>
	 * 使用rbacService获取.
	 * 
	 * @return 当前登录用户
	 */
	public static User getCurrentUser() {
		Assert.notNull(rbacService, "rbacService未注入！");

		return (User) rbacService.getCurrentUser();
	}

	/**
	 * 获取当前HttpServletRequest.
	 * <p/>
	 * 方便工具类访问request. Controller内推荐使用方法参数声明方式获取.
	 */
	public static HttpServletRequest getRequest() {
		final HttpServletRequest request = requestHolder.get();
		Assert.notNull(request, String.format(ENSURE_INJECT_MESSAGE, "request"));
		return request;
	}

	/**
	 * 获取当前HttpServletResponse.
	 * <p/>
	 * 方便工具类访问response. Controller内推荐使用方法参数声明方式获取.
	 */
	public static HttpServletResponse getResponse() {
		final HttpServletResponse response = responseHolder.get();
		Assert.notNull(response, String.format(ENSURE_INJECT_MESSAGE, "response"));
		return response;
	}

	/**
	 * 获取session.
	 */
	public static HttpSession getSession() {
		return getRequest().getSession();
	}

	public static void setRequestAndResponse(HttpServletRequest request, HttpServletResponse response) {
		requestHolder.set(request);
		responseHolder.set(response);
	}

	@Override
	public void destroy() throws Exception {
		cleanServiceListeners();
		cleanContexts();
	}

	public void cleanServiceListeners() {
		for (Class<?> serviceClass : serviceListeners.keySet()) {
			removeServiceListener(serviceClass);
		}
		serviceListeners.clear();
	}

	/**
	 * 清除applicationContext,servletContext静态变量.
	 */
	public static void cleanContexts() {
//		rbacService = null;
//		
//		for (ServiceReference<?> serviceReference : serviceReferences.values()) {
//			bundleContext.ungetService(serviceReference);
//		}
	}

	public void setRbacService(RbacService rbacService) {
		Application.rbacService = rbacService;
	}

	@Override
	public void setBundleContext(BundleContext bundleContext) {
		Application.bundleContext = bundleContext;
	}
}
