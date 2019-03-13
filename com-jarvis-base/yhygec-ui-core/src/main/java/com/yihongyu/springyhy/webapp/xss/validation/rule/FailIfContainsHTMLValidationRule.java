package com.yihongyu.springyhy.webapp.xss.validation.rule;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.text.Normalizer;
import java.util.Map;
import java.util.regex.Pattern;

import org.owasp.encoder.Encode;

import com.matthewcasperson.validation.exception.ValidationFailedException;
import com.matthewcasperson.validation.rule.ParameterValidationRuleTemplate;

/**
 * A validation rule that will cause the request to fail if the parameter includes any special
 * HTML characters
 * @author mcasperson
 */
public class FailIfContainsHTMLValidationRule extends ParameterValidationRuleTemplate {

	private static final Pattern XSS_REGEXP = Pattern.compile(
			".*(?:(<|\\%3c)(\\/|%2f|\\s|\\\u3000)*(script|img|javascript).*(>|%3e)|javascript(:|%3a)|(onblur|onchange|onfocus|onreset|onselect|onsubmit|onabort|onerror|onkeydown|onkeypress|onkeyup|onclick|ondblclick|onmousedown|onmousemove|onmouseout|onmouseover|onmouseup|onload|onunload|ondragdrop|onmove|onresize|style)=|content-type(:|%3a)).*",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final String ELLIPSIS = "…";

    private static final String ALLOW_AMPERSANDS = "allowAmpersands";
	private static final String ALLOW_ACCENTS = "allowAccents";
    private static final String ALLOW_ELLIPSIS = "allowEllipsis";
	private boolean allowAmpersands = false;
	private boolean allowAccents = false;
    private boolean allowEllipsis = false;

	/**
	 * {@inheritDoc}
	 */
	public void configure(final Map<String, String> settings) {
		if (settings.containsKey(ALLOW_AMPERSANDS)) {
			allowAmpersands = Boolean.parseBoolean(settings.get(ALLOW_AMPERSANDS));
		}

		if (settings.containsKey(ALLOW_ACCENTS)) {
			allowAccents = Boolean.parseBoolean(settings.get(ALLOW_ACCENTS));
		}

        if (settings.containsKey(ALLOW_ELLIPSIS)) {
            allowEllipsis = Boolean.parseBoolean(settings.get(ALLOW_ELLIPSIS));
        }
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] fixParams(final String name, final String url, final String[] params) throws ValidationFailedException {
		checkNotNull(name);
		checkArgument(!name.trim().isEmpty());
		checkNotNull(url);
		checkArgument(!url.trim().isEmpty());
		checkNotNull(params);
		checkArgument(params.length != 0, "PVF-BUG-0003: params should always have at least one value");

		for (int paramIndex = 0, paramLength = params.length; paramIndex < paramLength; ++paramIndex) {
			String param = params[paramIndex];

			if (allowAmpersands) {
				param = param.replaceAll("&", "");
			}

			if (allowAccents) {
                param = Normalizer.normalize(param, Normalizer.Form.NFD);
            }

            if (allowEllipsis) {
                param = param.replaceAll(ELLIPSIS, "");
            }
			
			if (param != null) {
				final String encoded = Encode.forHtml(param);
				
				if (!encoded.equals(param) || XSS_REGEXP.matcher(param).matches()) {
					throw new ValidationFailedException("PVF-SECURITY-0001: Parameter found to have special HTML characters.\nNAME: " + name + "\nVALUE: " + param + "\nURL: " + url);
				}
			}
		}
		
		return params;
	}
}