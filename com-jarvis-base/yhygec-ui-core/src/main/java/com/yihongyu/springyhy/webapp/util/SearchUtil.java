package com.yihongyu.springyhy.webapp.util;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.ServletRequestUtils;

import com.yihongyu.yhyweb.jpa.dao.Query;

/**
 * Utility Class for Search.
 */
public final class SearchUtil {
	private SearchUtil() {
		//
	}

	/**
	 * 需要分页
	 * 
	 * @param request
	 * @param query
	 * @return
	 */
	public static <T extends Serializable> Query<T, T> buildQuery(final HttpServletRequest request, final Query<T, T> query) {
		return SearchUtil.buildQuery(request, 20, query);
	}

	/**
	 * 不需要分页
	 * 
	 * @param request
	 * @param query
	 * @return
	 */
	public static <T extends Serializable> Query<T, T> buildNoPageQuery(final HttpServletRequest request, final Query<T, T> query) {
		return SearchUtil.buildQuery(request, false, 0, query);
	}

	/**
	 * 从Request请求参数自动构建查询对象. 用于 综合查询等.
	 */
	public static <T extends Serializable> Query<T, T> buildQuery(final HttpServletRequest request, int defaultPagesize, final Query<T, T> query) {
		return buildQuery(request, true, defaultPagesize, query);
	}
	
	/**
	 * 从Request请求参数自动构建查询对象. 用于 综合查询等.
	 */
	private static <T extends Serializable> Query<T, T> buildQuery(final HttpServletRequest request, Boolean isPage, int defaultPagesize, final Query<T, T> query) {

		if (isPage) {
			int page = ServletRequestUtils.getIntParameter(request, "page", 1);
			int pagesize = getIntParameter(request, defaultPagesize, "pagesize", "rows");
			boolean autoCount = ServletRequestUtils.getBooleanParameter(request, "autoCount", true);
			query.page(page, pagesize).autoCount(autoCount);
		} else {
			query.list();
		}

		sort(request, query);

		filter(request, query);

		return query;
	}

	/**
	 * 组合请求参数.
	 * <p/>
	 * 返回类似user=xx&page=1
	 */
	public static String searchParams2QueryString(HttpServletRequest request, String... excludedParams) {
		Map<String, String[]> params = getSearchParams(request, excludedParams);
		StringBuilder sb = new StringBuilder();
		for (Iterator<Map.Entry<String, String[]>> it = params.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, String[]> entry = it.next();
			String name = entry.getKey();
			String[] values = entry.getValue();

			if (values == null || values.length == 0) {
				sb.append(name).append("=");
			} else {
				for (int i = 0; i < values.length; i++) {
					if (i != 0) {
						sb.append("&");
					}
					sb.append(name).append("=").append(values[i]);
				}
			}

			if (it.hasNext()) {
				sb.append("&");
			}
		}
		return sb.toString();
	}

	public static int getIntParameter(HttpServletRequest request, int defaultValue, String name, String... mores) {
		if (request.getParameter(name) != null) {
			return ServletRequestUtils.getIntParameter(request, name, defaultValue);
		}
		for (String more : mores) {
			if (request.getParameter(more) != null) {
				return ServletRequestUtils.getIntParameter(request, more, defaultValue);
			}
		}
		return defaultValue;
	}

	public static String getStringParameter(HttpServletRequest request, String defaultValue, String name,
			String... mores) {
		if (request.getParameter(name) != null) {
			return ServletRequestUtils.getStringParameter(request, name, defaultValue);
		}
		for (String more : mores) {
			if (request.getParameter(more) != null) {
				return ServletRequestUtils.getStringParameter(request, more, defaultValue);
			}
		}
		return defaultValue;
	}

	private static void filter(HttpServletRequest request, Query<?, ?> query) {
		final Map<String, String[]> fparams = getFilterParams(request);
		for (Map.Entry<String, String[]> entry : fparams.entrySet()) {
			String name = entry.getKey();
			String[] values = entry.getValue();
			String[] names = name.split("_");

			String filterPrefix = names[0];
			String prop = names[1];
			String op = names.length > 2 ? names[2] : null; // 支持 start, end,
			// equal

			if ("f".equals(filterPrefix)) { // 字符类
				if (values.length > 1) { // 数组
					query.filterIn(prop, (Object[]) values); // in
				} else {
					if ("equal".equalsIgnoreCase(op)) { // equal support
						String value = values[0];
						query.filterEqual(prop, value); // equal
					} else {
						String value = values[0];
						query.filterILike(prop, "%" + value + "%"); // like
					}
				}
			} else if ("fd".equals(filterPrefix)) { // 日期类
				try {
					Date startValue = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(values[0] + " 00:00:00");
					Date endValue = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(values[0] + " 23:59:59");
					if (op == null || "equal".equalsIgnoreCase(op)) {

						query.filterGreaterOrEqual(prop, startValue).filterLessOrEqual(prop, endValue);
					} else if ("start".equalsIgnoreCase(op)) {
						query.filterGreaterOrEqual(prop, startValue); // great
						// than
					} else if ("end".equalsIgnoreCase(op)) {
						query.filterLessOrEqual(prop, endValue); // less
						// than
					}
				} catch (ParseException e) {
					//
				}
			} else if ("ft".equals(filterPrefix)) { // 时间类
				try {
					Date timeValue = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(values[0]);

					if (op == null || "equal".equalsIgnoreCase(op)) {

						query.filterEqual(prop, timeValue);
					} else if ("start".equalsIgnoreCase(op)) {
						query.filterGreaterOrEqual(prop, timeValue); // great
						// than
					} else if ("end".equalsIgnoreCase(op)) {
						query.filterLessOrEqual(prop, timeValue); // less
						// than
					}
				} catch (ParseException e) {
					//
				}
			} else if ("fn".equals(filterPrefix)) { // 数字类
				if (values.length > 1) { // 数组
					query.filterIn(prop, (Object[]) values); // in
				} else {
					String value = values[0];
					if (op == null || "equal".equalsIgnoreCase(op)) {
						query.filterEqual(prop, value); // equals
					} else if ("start".equalsIgnoreCase(op)) {
						query.filterGreaterOrEqual(prop, values[0]); // great
						// than
					} else if ("end".equalsIgnoreCase(op)) {
						query.filterLessOrEqual(prop, values[0]); // less
						// than
					}
				}
			}
		}
	}

	private static Map<String, String[]> getFilterParams(HttpServletRequest request) {
		Map<String, String[]> fparams = new HashMap<String, String[]>();
		Enumeration<String> names = request.getParameterNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			String[] values = request.getParameterValues(name);
			if (name.startsWith("f_") || name.startsWith("fd_") || name.startsWith("fn_") || name.startsWith("ft_")) {
				if (values != null && values.length > 0 && StringUtils.hasText(values[0])) {
					fparams.put(name, values);
				}
			}
		}
		return fparams;
	}

	private static boolean hasSort(Query<?, ?> query) {
		return query.toSearch().getSorts() != null || query.toSearch().getSorts().size() > 0;
	}

	private static void sort(HttpServletRequest request, Query<?, ?> query) {
		String sort = getStringParameter(request, "", "sort", "sidx");
		if (!StringUtils.hasText(sort)) {
			if (!hasSort(query)) {
				// 未指定排序条件, 则按id逆序.
				query.sortDesc("id");
			}
		} else {
			String dir = getStringParameter(request, "", "dir", "sord");
			if (StringUtils.hasText(dir)) {
				query.sort(sort, dir.equalsIgnoreCase("asc") ? false : true);
			} else {
				query.sortAsc(sort);
			}
		}
	}

	private static Map<String, String[]> getSearchParams(HttpServletRequest request, String... excludedParams) {
		Map<String, String[]> result = new HashMap<String, String[]>();
		Map<String, String[]> params = request.getParameterMap();
		for (Map.Entry<String, String[]> entry : params.entrySet()) {
			String name = entry.getKey();
			if (isIgnoreParam(name, excludedParams)) {
				continue;
			}
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	private static boolean isIgnoreParam(String name, String... excludedParams) {
		// 默认忽略 page
		final String[] es = excludedParams.length == 0 ? new String[] { "page" } : excludedParams;
		for (String e : es) {
			if (e.equals(name)) {
				return true;
			}
		}

		return false;
	}
}
