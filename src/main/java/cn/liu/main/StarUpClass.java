package cn.liu.main;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import cn.liu.configuration.SpringConfiguration;

/**
 * <li>主要入口类
 * 
 * @author liuliuliu
 *
 */
public class StarUpClass {
	
//	public static AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SpringConfiguration.class);

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringConfiguration.class);
//		ctx = new AnnotationConfigApplicationContext(SpringConfiguration.class);
//		AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(SpringConfiguration.class);
	}

}
