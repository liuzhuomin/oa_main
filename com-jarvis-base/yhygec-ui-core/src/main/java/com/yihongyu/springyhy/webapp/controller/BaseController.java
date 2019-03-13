package com.yihongyu.springyhy.webapp.controller;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomBooleanEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.core.convert.ConversionService;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.base.Optional;
import com.yihongyu.springyhy.user.entity.User;
import com.yihongyu.springyhy.webapp.Application;
import com.yihongyu.springyhy.webapp.util.SearchUtil;
import com.yihongyu.yhyweb.jpa.dao.Page;
import com.yihongyu.yhyweb.jpa.dao.Query;
import com.yihongyu.yhyweb.mvc.controller.AppController;
import com.yihongyu.yhyweb.mvc.result.Json;
import com.yihongyu.yhyweb.mvc.result.NotFound;
import com.yihongyu.yhyweb.mvc.util.Message;
import com.yihongyu.yhyweb.mvc.util.Messages.MessageBuilder;
import com.yihongyu.yhyweb.rbac.RbacService;

public abstract class BaseController extends AppController {

	protected static final String DATE_PATTERN = "yyyy-MM-dd";
	protected static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
	public static final String MODEL_RESULT_KEY = "result";
	public static final String QUERYSTRING_NAME = "queryString";
	public static final String RESOURCE_URI_NAME = "resuri";

	@Autowired
	private ServletContext servletContext;

	@Autowired
	private RbacService rbacService;

	@Autowired
	private Validator validator;

	@Autowired
	private ConversionService conversionService;

	private String[] _controllerMappings = null;

	protected ServletContext getServletContext() {
		return servletContext;
	}

	/**
	 * 首页
	 */
	@RequestMapping
	public String index(ModelMap model) {
		return view("/index");
	}

	/**
	 * 获取控制器RequestMapping value.
	 */
	protected final String[] getControllerMappings() {
		if (_controllerMappings == null) {
			final RequestMapping rm = this.getClass().getAnnotation(RequestMapping.class);

			_controllerMappings = rm != null ? rm.value() : new String[] {};
		}

		return _controllerMappings;
	}

	/**
	 * 获取控制器类第一个RequestMapping.
	 */
	protected final String getControllerFirstMapping() {
		final String[] controllerMappings = getControllerMappings();
		return controllerMappings.length == 0 ? null : controllerMappings[0];
	}

	/**
	 * 返回当前控制器指定文件的路径.
	 */
	protected String view(String path) {
		// request().setAttribute("ctx", request().getContextPath());
		String viewDir = getControllerFirstMapping(); // COC
		return viewDir + "/" + path;
	}

	/**
	 * 跳转viewname.
	 * 
	 */
	protected String redirect(String path) {
		return "redirect:" + (!path.startsWith("/") ? "/" + path : path);
	}

	/**
	 * 将request 请求参数存放到 ModelMap对象.
	 * 
	 * @param model
	 *            ModelMap
	 */
	protected void storeRequestParams(ModelMap model) {
		final HttpServletRequest request = Application.getRequest();
		Enumeration<String> names = request.getParameterNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			String[] values = request.getParameterValues(name);
			if (name.indexOf('.') != -1) {
				name = name.replaceAll("\\.", "_");
			}
			model.put(name, values.length > 1 ? values : values[0]);
		}
	}

	/**
	 * redirect 主页.
	 */
	protected String redirectIndex() {
		return redirect(getControllerFirstMapping());
	}

	/**
	 * redirect 显示特定项.
	 */
	protected String redirectShow(Long id) {
		return redirect(getControllerFirstMapping() + "/" + id);
	}

	/**
	 * 将query string存入model.
	 */
	protected void storeQueryString(ModelMap model) {
		model.put(QUERYSTRING_NAME, SearchUtil.searchParams2QueryString(Application.getRequest()));
	}

	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		registerCustomEidtorsForWebDataBinder(binder);
	}

	/**
	 * 自动绑定请求参数值到对应JavaBean.
	 * 
	 * 需参数名和属性名匹配.
	 * 
	 * @see #populateBeanFromRequest(Object, boolean)
	 */
	protected final BindingResult bind(Object bean) {
		final boolean custom = true;
		return bind(bean, custom);
	}

	/**
	 * 自动绑定请求参数值到对应JavaBean.
	 * 
	 * @param bean
	 *            JavaBean
	 * @param custom
	 *            是否采用controller 自定义数据帮定器.
	 * @return
	 */
	protected final BindingResult bind(Object bean, boolean custom) {
		ServletRequestDataBinder binder = new ServletRequestDataBinder(bean);

		binder.setValidator(validator);
		binder.setConversionService(conversionService);

		if (custom) {
			registerCustomEidtorsForWebDataBinder(binder);
		}

		binder.bind(request());
		binder.validate();

		return binder.getBindingResult();
	}

	/**
	 * 注册自定义类型转换器.
	 * 
	 */
	protected void registerCustomEidtorsForWebDataBinder(WebDataBinder binder) {
		// 日期格式化
		SimpleDateFormat dateFormat = new SimpleDateFormat(getBinderDatePattern());
		dateFormat.setLenient(false);
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
		// Boolean
		binder.registerCustomEditor(Boolean.class, new CustomBooleanEditor(true));
	}

	/**
	 * 自定义日期绑定格式.
	 */
	protected String getBinderDatePattern() {
		return "yyyy-MM-dd";
	}

	protected Optional<User> currentUser() {
		User currentUser = (User) rbacService.getCurrentUser();
		return Optional.<User> fromNullable(currentUser);
	}

	protected User ensureCurrentUser() {
		Optional<User> userOpt = currentUser();
		if (userOpt.isPresent()) {
			return userOpt.get();
		}
		throw new RuntimeException("获取当前登录用户失败!");
	}

	protected <T, RT> Page<RT> queryToPage(Query<T, RT> query) {
		return query.page(getPage(), getPagesize()).page();
	}

	private final String[] PageParamNames = { "page" };
	private final String[] PagesizeParamNames = { "rows", "limit", "pagesize" };

	protected int getPage() {
		int start = getIntParam("start", -1);
		if (start != -1) {
			int page = start / getPagesize() + 1;
			return page;
		}
		return getIntParam(PageParamNames, 1);
	}

	protected int getPagesize() {
		return getIntParam(PagesizeParamNames, 10);
	}

	protected int getIntParam(String[] paramNames, int defaultValue) {
		for (String name : paramNames) {
			try {
				return Integer.parseInt(request().getParameter(name));
			} catch (Exception e) {
				//
			}
		}

		return defaultValue;
	}

	protected int getIntParam(String paramName, int defaultValue) {
		return getIntParam(new String[] { paramName }, defaultValue);
	}

	protected boolean getBoolParam(String paramName, boolean defaultValue) {
		try {
			return Boolean.parseBoolean(request().getParameter(paramName));
		} catch (Exception e) {
			//
		}
		return defaultValue;
	}

	private static final NotFound notFound = new NotFound();

	protected NotFound NotFound() {
		return notFound;
	}

	protected Json messageAsJson(MessageBuilder builder) {
		return Json(builder.build());
	}

	protected Json messageAsJson(Message message) {
		return Json(message);
	}

	protected String formatDate(Timestamp date, String pattern) {
		return formatDate(new Date(date.getTime()), pattern);
	}

	protected String formatDate(Timestamp date) {
		return formatDate(date, DATE_PATTERN);
	}

	protected String formatDate(Date date) {
		return formatDate(date, DATE_PATTERN);
	}

	protected String formatDate(Date date, String pattern) {
		return new SimpleDateFormat(pattern).format(date);
	}

}
