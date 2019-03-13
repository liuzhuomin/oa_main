package com.yihongyu.springyhy.webapp.interceptor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Lists;
import com.yihongyu.yhyweb.mvc.plugin.AnnotationMethodHandlerPlugin;
import com.yihongyu.yhyweb.mvc.plugin.ContextAwarePlugin;
import com.yihongyu.yhyweb.mvc.plugin.FlashSupportPlugin;
import com.yihongyu.yhyweb.mvc.plugin.PluginProcess;
import com.yihongyu.yhyweb.mvc.result.Empty;
import com.yihongyu.yhyweb.mvc.result.JSPTemplate;
import com.yihongyu.yhyweb.mvc.result.Json;
import com.yihongyu.yhyweb.mvc.result.Result;
import com.yihongyu.yhyweb.mvc.result.Results;
import com.yihongyu.yhyweb.mvc.util.flow.Interrupt;
import com.yihongyu.yhyweb.mvc.util.flow.InvokeFlow;
import com.yihongyu.yhyweb.mvc.util.flow.InvokeFlows;

public class GecAnnotationMethodHandlerAdapter extends AbstractGecAnnotationMethodHandlerAdapter {
	private static Logger logger = LoggerFactory.getLogger(GecAnnotationMethodHandlerAdapter.class);

	private List<AnnotationMethodHandlerPlugin> plugins = Lists.newArrayList();

	private final List<AnnotationMethodHandlerPlugin> defaultPlugins = Arrays.asList(
			(AnnotationMethodHandlerPlugin) new ContextAwarePlugin(), new FlashSupportPlugin());

	@Override
	public ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response,
			final Object handler) {
		logger.debug("The Handler: {}", handler.getClass().getName());
		try {
			pluginsProcess(new PluginProcess() {
				@Override
				public InvokeFlow execute(AnnotationMethodHandlerPlugin plugin) {
					return plugin.beforeHandle(request, response, handler);
				}
			});

			// 调用父类处理逻辑
			ModelAndView modelAndView = super.handle(request, response, handler);

			if (modelAndView != null) {
				logger.debug("After handle, the view: {}", modelAndView.getViewName());
			}

			pluginsProcess(new PluginProcess() {
				@Override
				public InvokeFlow execute(AnnotationMethodHandlerPlugin plugin) {
					return plugin.postHandle(request, response, handler);
				}
			});
			return modelAndView;
		} catch (FlowException e) { // 中断执行flow
			logger.info("Execute flow interrupted");
			return e.getFlow().getModelAndView();
		} catch (Throwable e) { // 捕获所有的异常
			e.printStackTrace();
			logger.error("执行出错!", e);
			ModelAndView mv = new ModelAndView("/uncaughtException");
			mv.getModel().put("exception", e);
			mv.getModel().put("title", "未知错误");
			return mv;
		}
	}

	@SuppressWarnings("serial")
	static class FlowException extends Exception {

		private final InvokeFlow flow;

		public FlowException(InvokeFlow flow) {
			this.flow = flow;
		}

		public InvokeFlow getFlow() {
			return flow;
		}
	}

	@Override
	public InvokeFlow beforeInvokeHandlerMethod(final HttpServletRequest request, final HttpServletResponse response,
			final Object handler, final Method handlerMethod) {
		logger.debug("The handle method: {}", handlerMethod);
		try {
			pluginsProcess(new PluginProcess() {
				@Override
				public InvokeFlow execute(AnnotationMethodHandlerPlugin plugin) {
					return plugin.beforeInvokeHandlerMethod(request, response, handler, handlerMethod);
				}
			});
		} catch (FlowException e) {
			return e.getFlow();
		}

		return InvokeFlows.CONTINUE;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected InvokeFlow afterInvokeHandlerMethod(final HttpServletRequest request, final HttpServletResponse response,
			final Object handler, final Method handlerMethod, final Object invokeResult, ExtendedModelMap implicitModel) {
		logger.debug("The handle method return value: {}", invokeResult);
		try {
			pluginsProcess(new PluginProcess() {
				@Override
				public InvokeFlow execute(AnnotationMethodHandlerPlugin plugin) {
					return plugin.postInvokeHandlerMethod(request, response, handler, handlerMethod, invokeResult);
				}
			});
		} catch (FlowException e) {
			return e.getFlow();
		}

		// 如果action方法定义为void则表示无需处理
		if (handlerMethod.getReturnType() == void.class) {
			return new Interrupt(resultToModelAndView(Empty.instance));
		}

		// 如过action返回的是Result或null, 拦截使其进入Result的处理逻辑
		if (invokeResult == null) {
			// return new Interrupt(resultToModelAndView(Empty.instance));
			return new Interrupt(resultToModelAndView(new Json(implicitModel)));

		} else if (invokeResult instanceof JSPTemplate) {
			JSPTemplate result = (JSPTemplate) invokeResult;
			return new Interrupt(new ModelAndView(result.getTemplate(), result.getValues()));
		} else if (invokeResult instanceof Result) {
			return new Interrupt(resultToModelAndView((Result) invokeResult));
		}

		return InvokeFlows.CONTINUE;
	}

	private void pluginsProcess(PluginProcess process) throws FlowException {
		for (AnnotationMethodHandlerPlugin plugin : allPlgins()) {
			InvokeFlow flow = process.execute(plugin);
			if (!flow.isContinue()) {
				throw new FlowException(flow);
			}
		}
	}

	private List<AnnotationMethodHandlerPlugin> _allPlugins = null;

	private List<AnnotationMethodHandlerPlugin> allPlgins() {
		if (_allPlugins == null) {
			List<AnnotationMethodHandlerPlugin> result = sortPlugins(getDefaultPlugins());
			if (getPlugins() != null) {
				List<? extends AnnotationMethodHandlerPlugin> plugins = sortPlugins(getPlugins());
				result.addAll(plugins);
			}
			_allPlugins = result;
		}
		return _allPlugins;
	}

	private List<AnnotationMethodHandlerPlugin> sortPlugins(List<AnnotationMethodHandlerPlugin> plugins) {
		Collections.sort(plugins, new Comparator<Ordered>() {
			@Override
			public int compare(Ordered o1, Ordered o2) {
				return o1.getOrder() - o1.getOrder();
			}
		});
		return plugins;
	}

	protected final List<AnnotationMethodHandlerPlugin> getDefaultPlugins() {
		return defaultPlugins;
	}

	public List<AnnotationMethodHandlerPlugin> getPlugins() {
		return plugins;
	}

	public void setPlugins(List<AnnotationMethodHandlerPlugin> plugins) {
		this.plugins = plugins;
	}

	private ModelAndView resultToModelAndView(final Result result) {
		ModelAndView mv = new ModelAndView(Results.viewName(result));
		mv.getModel().put(Result.NAME, result);
		return mv;
	}
}
