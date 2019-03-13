package cn.liu.annotions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <Li>������ע����,�û���ȡ������
 * @author liuliuliu
 *
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface ListenerAnnotion {
	/**
	 * <li>ͨ�����ͻ�ȡ
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	Class[] value() default {};
	
	/**
	 * <li>ͨ�����ƻ�ȡ
	 * @return
	 */
	String[] name() default "";
}
