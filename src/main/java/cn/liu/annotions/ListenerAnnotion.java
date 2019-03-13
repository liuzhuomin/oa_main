package cn.liu.annotions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <Li>监听器注解类,用户获取监听器
 * @author liuliuliu
 *
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface ListenerAnnotion {
	/**
	 * <li>通过类型获取
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	Class[] value() default {};
	
	/**
	 * <li>通过名称获取
	 * @return
	 */
	String[] name() default "";
}
