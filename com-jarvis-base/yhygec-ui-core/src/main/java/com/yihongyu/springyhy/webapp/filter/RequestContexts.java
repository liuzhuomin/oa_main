package com.yihongyu.springyhy.webapp.filter;

import javax.servlet.http.HttpServletRequest;

import com.yihongyu.springyhy.user.entity.User;
import com.yihongyu.springyhy.webapp.Application;
import com.yihongyu.springyhy.webapp.util.Strings;

public final class RequestContexts {
	public static final String REQUEST_CONTEXT_KEY = "requestContext";
	public static final String CONTEXT_PATH_KEY = "ctx";
	public static final String FORMAT_DATE_KEY = "formatDate";
	public static final String FORMAT_DATE_TIME_KEY = "formatDateTime";

	private RequestContexts() {
	}

	public static void makeRequestContext(final HttpServletRequest request) {
		request.setAttribute(RequestContexts.CONTEXT_PATH_KEY, request.getContextPath()); // 上下文路径
		final RequestContext requestContext = new DefaultRequestContext(request); // 设置最初请求路径.
		request.setAttribute(FORMAT_DATE_KEY, "yyyy-MM-dd");
		request.setAttribute(FORMAT_DATE_TIME_KEY, "yyyy-MM-dd HH:mm:ss");
		request.setAttribute(REQUEST_CONTEXT_KEY, requestContext);
	}

	public static RequestContext currRequestContext() {
		return getAttribute(REQUEST_CONTEXT_KEY);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getAttribute(String key) {
		return (T) getAttribute(Application.getRequest(), key);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getAttribute(HttpServletRequest request, String key) {
		return (T) request.getAttribute(key);
	}

	static class DefaultRequestContext implements RequestContext {
		private final HttpServletRequest request;
		private final String requestURI;
		private User _currentUser;

		public DefaultRequestContext(HttpServletRequest request) {
			this.request = request;
			requestURI = request.getRequestURI();
		}

		@Override
		public String getRequestURI() {
			return requestURI;
		}

		@Override
		public User getCurrentUser() {
			if (_currentUser == null) {
				_currentUser = Application.getCurrentUser();
			}
			return _currentUser;
		}

		@Override
		public String getContextPath() {
			return request.getContextPath();
		}

		@Override
		public String getModulePath() {
			return Strings.takeUntil(Strings.dropHead(getRequestURI(), getContextPath()), '/', 2);
		}

		@Override
		public String getMenuPath() {
			return Strings.dropHead(getRequestURI(), getContextPath(), getModulePath());
		}
	}
}
