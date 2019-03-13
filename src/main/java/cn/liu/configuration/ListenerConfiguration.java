package cn.liu.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.liu.listener.MyKeyListener;
import cn.liu.listener.MyMouseListener;

/**
 * <li>监听器实现类,将所有的监听器集中为一个监听器,并且痛楚指定标识来判断
 * @author liuliuliu
 *
 */
@Configuration
public class ListenerConfiguration {
	
	/**
	 * <li>我的鼠标监听器
	 * @return
	 */
	@Bean
	public org.eclipse.swt.events.MouseListener mouseListener(){
		return new MyMouseListener();
	}
	/**
	 * <li>我的键盘监听器
	 * @return
	 */
	@Bean
	public org.eclipse.swt.events.KeyListener keyListener(){
		return new MyKeyListener();
	}
}
