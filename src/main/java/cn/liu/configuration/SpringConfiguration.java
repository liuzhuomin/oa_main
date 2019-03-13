package cn.liu.configuration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import cn.liu.annotions.ListenerAnnotion;
import cn.liu.main.PatrolClient;
import cn.liu.utils.ApplicationContextBeanUtil;
import cn.liu.utils.PackageUtils;

@Configuration
@ComponentScan(basePackages = "cn.liu")
@Import({ ListenerConfiguration.class, ComponentConfiguration.class, InterFaceConfiguration.class })
public class SpringConfiguration {

	@Autowired
	KeyListener keyListener;

	@Autowired
	Composite loginComposite;

	/**
	 * <li>初始化SWT窗口,并且初始化方法为open
	 * 
	 * @return
	 */
	@Bean(initMethod = "open")
	public PatrolClient patrolClient() {
		PatrolClient patrolClient = new PatrolClient();// 初始化组件
		addListeners(); // 初始化监听器
		return patrolClient;
	}

	@Bean
	public ApplicationContextBeanUtil ctxUtil() {
		return new ApplicationContextBeanUtil();
	}
	
	/**
	 * <li>为组件对象添加监听器
	 */
	private void addListeners() {
		Control[] children = loginComposite.getChildren();
		loginComposite.addKeyListener(keyListener);
		for (Control control : children) {
			control.addKeyListener(keyListener);
		}
		addAnnotionsListener();
	}

	/**
	 * <li>添加指定注解的指定所有组件的监听器
	 */
	private void addAnnotionsListener() {
		List<Class<ListenerAnnotion>> list = new ArrayList<>();
		list.add(ListenerAnnotion.class);
		addAllListenerByAannotions(list);
	}

	/**
	 * <li>通过集合中的注解类型为bean添加监听器
	 * @param list
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void addAllListenerByAannotions(List<Class<ListenerAnnotion>> list) {
		List<Class> classsFromPackage = PackageUtils.getClasssFromPackage("cn.liu", list);
		for (Class class1 : classsFromPackage) {
			Method[] methods = class1.getMethods();
			for (Method method : methods) {
				for (int i = 0; i < list.size(); i++) {
					if (method.isAnnotationPresent(list.get(i))) { // 如果是此类型的注解
						String name = method.getName();
						Object bean = ApplicationContextBeanUtil.getBean(name);
						if (bean instanceof Control) {
							ListenerAnnotion annotation = method.getAnnotation(list.get(i)); // 获取当前注解
							Class[] value = annotation.value();
							Control control = (Control) bean;
							for (Class annoClass : value) {
								Object listener = ApplicationContextBeanUtil.getBean(annoClass);
								joinIfRight(control, annoClass, listener);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * <li>如果此监听器类型存在则添加到当前组件
	 * @param control   组件类对象
	 * @param annoClass 注解上的class对象
	 * @param listener  监听器对象
	 */
	@SuppressWarnings("rawtypes")
	private void joinIfRight(Control control, Class annoClass, Object listener) {
		if (annoClass == MouseListener.class) {
			control.addMouseListener((MouseListener) listener);
		}

		if (annoClass == KeyListener.class) {
			control.addKeyListener((KeyListener) listener);
		}
	}

}
