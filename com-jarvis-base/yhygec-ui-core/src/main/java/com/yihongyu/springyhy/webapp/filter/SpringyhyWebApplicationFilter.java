package com.yihongyu.springyhy.webapp.filter;

import java.io.IOException;
import java.util.ArrayList;
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

import com.yihongyu.springyhy.webapp.Application;
import com.yihongyu.yhyweb.util.web.uilt.WebUtils;

public class SpringyhyWebApplicationFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(SpringyhyWebApplicationFilter.class);

	private List<Filter> internalFilters = new ArrayList<Filter>();

	public SpringyhyWebApplicationFilter() {
		internalFilters.add(new CsrfFilter());
		internalFilters.add(XssFilter.instance());
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		for (Filter filter : internalFilters) {
			filter.init(filterConfig);
		}
		BundleContext bundleContext = WebUtils.getBundleContext(filterConfig.getServletContext());
		System.out.println(" ");
		System.out.println("*=====================================*");
		System.out.println("|        " + bundleContext.getBundle().getSymbolicName() + " 初始化成功        |");
		System.out.println("*=====================================*");
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		final HttpServletRequest request = (HttpServletRequest) servletRequest;
		final HttpServletResponse response = (HttpServletResponse) servletResponse;

		if (beforeFilter(request, response)) {
			new DelegatingFilterChain(filterChain, internalFilters).doFilter(request, response);
			afterFilter(request, response);
		}
	}

	@Override
	public void destroy() {
//		Application.cleanContexts();
	}

	protected boolean beforeFilter(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// inject request response
		Application.setRequestAndResponse(request, response);
		injectContextParams(request);
		return true;
	}

	protected void afterFilter(HttpServletRequest request, HttpServletResponse response) {
	}

	private void injectContextParams(HttpServletRequest request) {
		RequestContexts.makeRequestContext(request);

		flashToRequestContext(request);
	}

	private void flashToRequestContext(HttpServletRequest request) {
//		HttpSession session = request.getSession(false);
	}

    private final class DelegatingFilterChain implements FilterChain {

        private FilterChain orig;
        private List<Filter> filters;
        private int index = 0;

        public DelegatingFilterChain(FilterChain orig, List<Filter> filters) {
            if (orig == null) {
                throw new NullPointerException("original FilterChain cannot be null.");
            }
            this.orig = orig;
            this.filters = filters;
            this.index = 0;
        }

        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            if (this.filters == null || this.filters.size() == this.index) {
                //we've reached the end of the wrapped chain, so invoke the original one:
                if (logger.isTraceEnabled()) {
                    logger.trace("Invoking original filter chain.");
                }
                this.orig.doFilter(request, response);
            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace("Invoking Delegated filter at index [" + this.index + "]");
                }
                this.filters.get(this.index++).doFilter(request, response, this);
            }
        }
        
    }
}
