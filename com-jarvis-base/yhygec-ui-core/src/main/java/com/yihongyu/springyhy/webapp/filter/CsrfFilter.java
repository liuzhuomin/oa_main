package com.yihongyu.springyhy.webapp.filter;

import java.io.IOException;  
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;  
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;  
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;  
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.yihongyu.yhyweb.util.web.uilt.WebUtils;

public class CsrfFilter implements Filter {

	private static final Logger LOGGER = LoggerFactory.getLogger(CsrfFilter.class);

	private static final String SUPPORTED_REFERER_PROP = "filter.csrf.supportedReferers";

	private List<String> supportedReferers;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		BundleContext bundleContext = WebUtils.getBundleContext(filterConfig.getServletContext());
		String supported_referers = bundleContext.getProperty(SUPPORTED_REFERER_PROP);
		supportedReferers = StringUtils.isEmpty(supported_referers) ? new ArrayList<String>() : Arrays.asList(supported_referers.split(";"));
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (accept((HttpServletRequest) request)) {
			chain.doFilter(request, response);
		} else {
			((HttpServletResponse) response).sendError(400, "非法请求");
		}
	}

	@Override
	public void destroy() {
	}

	protected boolean accept(HttpServletRequest request) throws ServletException {
		String referer = request.getHeader("Referer");
		String server = request.getScheme() + "://" + request.getServerName(); // + ":" + request.getServerPort();
		if(referer!=null && referer.startsWith("http://ns.szxrhl.cn/")) {
			return true;
		}
		if (referer == null || referer.startsWith(server)) {
			return true;
		}
		for (String expected : supportedReferers) {
			if (referer.startsWith(expected)) {
				return true;
			}
		}
		LOGGER.error("rejected - referer: " + referer);

		return false;
	}
}
