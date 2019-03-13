package com.yihongyu.springyhy.webapp.filter;

import com.yihongyu.springyhy.user.entity.User;

/**
 * 请求Context.
 * 
 */
public interface RequestContext {
	/**
	 * 最初请求URI.
	 */
	String getRequestURI();

	/**
	 * 当前用户.
	 */
	User getCurrentUser();
	
	String getModulePath();
	
	String getContextPath();
	
	String getMenuPath();
}
