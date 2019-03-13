package com.yihongyu.springyhy.webapp.filter;

import java.io.IOException;  

import javax.servlet.Filter;
import javax.servlet.FilterChain;  
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;  
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;  

import org.springframework.util.StringUtils;

public class OldXssFilter implements Filter {

	private static String IGNORE_URL_INIT_PARAMETER = "ignoreUrls";

	private String[] ignoreUrls;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		String ignore_urls = filterConfig.getInitParameter(IGNORE_URL_INIT_PARAMETER);
		ignoreUrls = StringUtils.isEmpty(ignore_urls) ? new String[] {} : ignore_urls.split(",");
		System.out.println("[XssFilter] init - ignoreUrls: " + ignoreUrls);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (shouldNotFilter((HttpServletRequest) request)) {
			chain.doFilter(request, response);
		} else {
			chain.doFilter(new XssHttpServletRequestWrapper((HttpServletRequest) request), response);
		}
	}

	@Override
	public void destroy() {
	}

	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		String requestURI = request.getServletPath();
		if (requestURI == null) {
			return false;
		}

		for (String ignoreUrl : ignoreUrls) {
			if (requestURI.startsWith(ignoreUrl)) {
				System.out.println("[XssFilter] ignore - requestURI: " + requestURI);
				return true;
			}
		}

		return false;
	}
}
