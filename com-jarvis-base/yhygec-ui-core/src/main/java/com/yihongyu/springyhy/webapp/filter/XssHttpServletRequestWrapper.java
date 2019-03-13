package com.yihongyu.springyhy.webapp.filter;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.json.JSONException;
import org.json.JSONObject;

public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {
	public XssHttpServletRequestWrapper(HttpServletRequest servletRequest) {
		super(servletRequest);
	}

	public String[] getParameterValues(String parameter) {
		String[] values = super.getParameterValues(parameter);
		if (values == null) {
			return null;
		}
		int count = values.length;
		String[] encodedValues = new String[count];
		for (int i = 0; i < count; i++) {
			encodedValues[i] = cleanXSS(values[i]);
		}
		return encodedValues;
	}

	public String getParameter(String parameter) {
		String value = super.getParameter(parameter);
		if (value == null) {
			return null;
		}
		// ignore json object
		if (value.startsWith("{") || value.startsWith("[")) {
			try {
				new JSONObject(value);
				return value;
			} catch (JSONException e) {
				//ignore
			}
		}
		return cleanXSS(value);
	}

	public String getHeader(String name) {
		String value = super.getHeader(name);
		if (value == null)
			return null;
		return cleanXSS(value);
	}

    /**
     * XSS特殊字符过滤，防止XSS攻击
     * @param value
     * @return
     */
	protected String cleanXSS(String value) {
		value = value.replaceAll("<", "＜").replaceAll(">", "＞")
					.replaceAll("\\(", "（").replaceAll("\\)", "）")
					.replaceAll("'", "’")
					.replaceAll("\"", "“")
					.replaceAll("`", "’")
					.replaceAll("eval\\((.*)\\)", "")
					.replaceAll("[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']", "\"\"")

					.replaceAll("(?i)<script.*?>.*?<script.*?>", "")
					.replaceAll("(?i)<script.*?>.*?</script.*?>", "")
					.replaceAll("(?i)<.*?javascript:.*?>.*?</.*?>", "")
					.replaceAll("(?i)<.*?\\s+on.*?>.*?</.*?>", "")
					.replaceAll("<script>", "")
					.replaceAll("</script>", "");

		// Avoid anything in a src='...' type of expression
		Pattern scriptPattern = Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		value = scriptPattern.matcher(value).replaceAll("");

		scriptPattern = Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		value = scriptPattern.matcher(value).replaceAll("");		

		// Avoid anything in a iframe='...' type of expression
		scriptPattern = Pattern.compile("iframe[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		value = scriptPattern.matcher(value).replaceAll("");

		scriptPattern = Pattern.compile("iframe[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		value = scriptPattern.matcher(value).replaceAll("");		

		return value;
	}
}