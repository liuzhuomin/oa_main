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
	 * <li>��ʼ��SWT����,���ҳ�ʼ������Ϊopen
	 * 
	 * @return
	 */
	@Bean(initMethod = "open")
	public PatrolClient patrolClient() {
		PatrolClient patrolClient = new PatrolClient();// ��ʼ�����
		addListeners(); // ��ʼ��������
		return patrolClient;
	}

	@Bean
	public ApplicationContextBeanUtil ctxUtil() {
		return new ApplicationContextBeanUtil();
	}
	
	/**
	 * <li>Ϊ���������Ӽ�����
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
	 * <li>���ָ��ע���ָ����������ļ�����
	 */
	private void addAnnotionsListener() {
		List<Class<ListenerAnnotion>> list = new ArrayList<>();
		list.add(ListenerAnnotion.class);
		addAllListenerByAannotions(list);
	}

	/**
	 * <li>ͨ�������е�ע������Ϊbean��Ӽ�����
	 * @param list
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void addAllListenerByAannotions(List<Class<ListenerAnnotion>> list) {
		List<Class> classsFromPackage = PackageUtils.getClasssFromPackage("cn.liu", list);
		for (Class class1 : classsFromPackage) {
			Method[] methods = class1.getMethods();
			for (Method method : methods) {
				for (int i = 0; i < list.size(); i++) {
					if (method.isAnnotationPresent(list.get(i))) { // ����Ǵ����͵�ע��
						String name = method.getName();
						Object bean = ApplicationContextBeanUtil.getBean(name);
						if (bean instanceof Control) {
							ListenerAnnotion annotation = method.getAnnotation(list.get(i)); // ��ȡ��ǰע��
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
	 * <li>����˼��������ʹ�������ӵ���ǰ���
	 * @param control   ��������
	 * @param annoClass ע���ϵ�class����
	 * @param listener  ����������
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
