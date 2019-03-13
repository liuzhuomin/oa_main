package cn.liu.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.liu.listener.MyKeyListener;
import cn.liu.listener.MyMouseListener;

/**
 * <li>������ʵ����,�����еļ���������Ϊһ��������,����ʹ��ָ����ʶ���ж�
 * @author liuliuliu
 *
 */
@Configuration
public class ListenerConfiguration {
	
	/**
	 * <li>�ҵ���������
	 * @return
	 */
	@Bean
	public org.eclipse.swt.events.MouseListener mouseListener(){
		return new MyMouseListener();
	}
	/**
	 * <li>�ҵļ��̼�����
	 * @return
	 */
	@Bean
	public org.eclipse.swt.events.KeyListener keyListener(){
		return new MyKeyListener();
	}
}
