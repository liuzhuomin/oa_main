package com.yihongyu.springyhy.webapp.filter;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;  
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;  
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;  
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;  
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.matthewcasperson.validation.exception.ValidationFailedException;
import com.matthewcasperson.validation.rule.ParameterValidationRule;
import com.matthewcasperson.validation.ruledefinitionimpl.ParameterValidationChain;
import com.matthewcasperson.validation.ruledefinitionimpl.ParameterValidationDefinitionImpl;
import com.matthewcasperson.validation.ruledefinitionimpl.ParameterValidationDefinitionsImpl;
import com.matthewcasperson.validation.utils.SerialisationUtils;
import com.matthewcasperson.validation.utilsimpl.JaxBSerialisationUtilsImpl;
import com.yihongyu.yhyweb.util.web.uilt.WebUtils;

public class XssFilter implements Filter {
	private static final Logger LOGGER = LoggerFactory.getLogger(XssFilter.class);
	private static final SerialisationUtils SERIALISATION_UTILS = new JaxBSerialisationUtilsImpl();
	private static final String CONFIG_FILE_NAME = "pvf.xml";
	private static final String PROP_IGNORE_URLS_PROP = "filter.xss.ignoreUrls";

	private String[] ignoreUrls;

	/**
	 * The list of validation rules that are to be applied 
	 */
	private ParameterValidationDefinitionsImpl parameterValidationDefinitions;

	private static XssFilter instance;

	private XssFilter() {
	}

	synchronized public static XssFilter instance() {
		if (instance == null) {
			instance = new XssFilter();
		}
		return instance;
	}

	@Override
	public void destroy() {
		/*
		 * Nothing to do here
		 */
	}

	/**
	 * This filter implements multiple chains of validation rules. Each chain is executed against each parameter until 
	 * alll validation rules have been executed, or until one of the validation rules stops the execution of the chain.
	 */
	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		
		//LOGGER.log(Level.INFO, "Parameter Validation Filter processing request");
		
		ServletRequest requestWrapper = request;
		
		try {		
			if (parameterValidationDefinitions != null && parameterValidationDefinitions.getParameterValidationDefinitions() != null) {
				
				LOGGER.debug("Parameter Validation Filter has loaded the config file");
				
				if (requestWrapper instanceof HttpServletRequest && !shouldNotFilter((HttpServletRequest) request)) {
					
					//LOGGER.log(Level.INFO, "Parameter Validation Filter is filtering a HttpServletRequest");
					
					final HttpServletRequest httpServletRequest = (HttpServletRequest)requestWrapper;

					String uri = httpServletRequest.getRequestURI();
					if (uri.indexOf(";") >= 0 || uri.indexOf(",") >= 0) {
						throw new ValidationFailedException("Invalid request url: " + uri);
					}

					/*
					 * Loop over each param. Note that while the validation rules may well
					 * create wrappers that return different values for the params (i.e. requestWrapper is
					 * updated to reference a new wrapper), we use this original copy for the list of 
					 * param keys to loop over.
					 */
					final Enumeration<String> iter = httpServletRequest.getParameterNames();

					paramaterNameLoop:
					while (iter.hasMoreElements()) {
						/*
						 * Get the param name and move the enumerator along
						 */
						final String paramName = iter.nextElement();
						if ("password".equals(paramName) || !paramName.equals(Encode.forHtml(paramName))) {
							throw new ValidationFailedException("Invalid parameter name: " + paramName);
						}

						boolean paramValidated = false;
						
						LOGGER.debug("Parameter Validation Filter processing " + paramName);
						
						/*
						 * Loop over each validation rule in the chain
						 */
						final List<ParameterValidationChain> validationChains = parameterValidationDefinitions.getParameterValidationDefinitions();
						for (final ParameterValidationChain validationChain : validationChains) {
							
							checkState(validationChain != null, "A validation rule should never be null");
							
							/*
							 * Test this validation rule against the param name
							 */
							
							final boolean paramMatches = validationChain.getParamNamePattern().matcher(paramName).find();
							final boolean uriMatches = validationChain.getRequestURIPattern().matcher(httpServletRequest.getRequestURI()).find();
																				
							final boolean paramMatchesAfterNegation = paramMatches ^ validationChain.isParamNamePatternNegated();
							final boolean uriMatchesAfterNegation = uriMatches ^ validationChain.isRequestURIPatternNegated();
							
							if (paramMatchesAfterNegation && uriMatchesAfterNegation) {
								
								LOGGER.debug("Parameter Validation Filter found matching chain");

								/*
								 * Make a note that this parameter has been validated by at least one rule
								 */
								paramValidated = true;
								
								/*
								 * Loop over each rule in the chain 
								 */
								for (final ParameterValidationDefinitionImpl validationRule : validationChain.getList()) {
									LOGGER.debug("Processing " + paramName + " with " + validationRule.getValidationRuleName());
									
									/*
									 * Get the object that will actually do the validation
									 */
									final ParameterValidationRule rule = validationRule.getRule();
									
									/*
									 * It is possible that a bad configuration will result in rule being null
									 */
									checkState(rule != null, "A validation rule should never be null. Check the class name defined in the configuration xml file.");

									try {
										/*
											Process the parameter
										 */
										final ServletRequest processRequest = rule.processParameter(requestWrapper, paramName);

										checkState(processRequest != null, "A validation rule should never return null when processing a paramemter");

										/*
										 * The validation rule is expected to return a valid request regardless of the
										 * processing that should or should not be done.
										 */
										requestWrapper = processRequest;
									} catch (final ValidationFailedException ex) {
										/*
										 * Log this as a warning as we are probably interested in knowing when our apps
										 * are getting hit with invalid data.
										 */
										LOGGER.warn(ex.toString());


										if (parameterValidationDefinitions.getEnforcingMode()) {
											/*
												If we are enforcing, rethrow so the outer catch block can block the
												request
										 	*/
											throw ex;
										} else {
											/*
												Otherwise move to the next parameter name. This allows us to be notified
												of every param that will fail instead of just bailing with the first
												one that fails.
											 */
											continue paramaterNameLoop;
										}
									}

								}
							}
						}

						if (!paramValidated) {
							/*
								 * This might be intentional, so log it as an INFO
								 */
							LOGGER.info("PVF-INFO-0001: " + paramName + " has not been validated.");
						}
					}					
				}
			}			
		} catch (final ValidationFailedException ex) {					
			/*
			 * Stop processing and return a HTTP error code if we are enforcing the rules
			 */
			if (parameterValidationDefinitions != null &&
                    parameterValidationDefinitions.getEnforcingMode()) {
				respondWithBadRequest(response);
				return;
			}
		}
		catch (final Exception ex) {
			/*
			 * We probably reach this because of some invalid state due to rules returning null
			 * or throwing unchecked exceptions during their own processing. This is logged as
			 * severe as it is most likely a bug in the code.
			 */
			LOGGER.error(ExceptionUtils.getFullStackTrace(ex));	
			
			/*
			 * Don't allow apps to process raw parameters if this filter has failed and we are
			 * enforcing the rules
			 */
			if (parameterValidationDefinitions != null &&
                    parameterValidationDefinitions.getEnforcingMode()) {
				respondWithBadRequest(response);
				return;
			}
		}
		
		/*
		 * Continue to the next filter
		 */
		if (parameterValidationDefinitions != null &&
                parameterValidationDefinitions.getEnforcingMode()) {
			/*
				In enforcing mode we pass the wrapper onto the next filter
			 */
			chain.doFilter(requestWrapper, response);
		} else {
			/*
				If enforcing mode is not enabled, we pass through the original request
			 */
			chain.doFilter(request, response);
		}
	}
	
	/**
	 * Return with a status code of 400
	 * @param response The servlet request
	 */
	private void respondWithBadRequest(final ServletResponse response) {
		checkNotNull(response);
		
		/*
		 * This is thrown when one of the validation rules determined that a parameter was
		 * sent with invalid data and could not, or should not, be sanitised.
		 */
		if (response instanceof HttpServletResponse) {
			try {
				final HttpServletResponse httpServletResponse = (HttpServletResponse)response;
				httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter data");
			} catch (final IOException ex) {
				/*
				 * This shouldn't happen, but log it if it does
				 */
				LOGGER.error(ExceptionUtils.getFullStackTrace(ex));	
			}
		}
	}

	/**
	 * Attempts to parse the XML config file. The config file is a JaxB serialisation of a
	 * ParameterValidationDefinitionsImpl object. 
	 */
	@Override
	public void init(final FilterConfig config) throws ServletException {
		BundleContext bundleContext = WebUtils.getBundleContext(config.getServletContext());
		String ignore_urls = bundleContext.getProperty(PROP_IGNORE_URLS_PROP);
		ignoreUrls = StringUtils.isEmpty(ignore_urls) ? new String[] {} : ignore_urls.split(";");
		if (parameterValidationDefinitions == null) {
			doInit();
		}
	}

	protected void doInit() throws ServletException {
		try {
			Bundle bundle = FrameworkUtil.getBundle(XssFilter.class);
			URL entry = bundle.getEntry(CONFIG_FILE_NAME);
			if (entry != null) {			
				LOGGER.debug("Attempting to unmarshall " + CONFIG_FILE_NAME);
				final String configXml = IOUtils.toString(entry.openStream());
				LOGGER.debug("configXml is \n" + configXml);
				parameterValidationDefinitions = SERIALISATION_UTILS.readFromXML(configXml, ParameterValidationDefinitionsImpl.class);
			}
		} catch (final Exception ex) {
			/*
			 * This will happen if the supplied XML is invalid. Log the error
			 */
			LOGGER.error(ExceptionUtils.getFullStackTrace(ex));
			
			/*
			 * Rethrow as we don't want to proceed with invalid configuration
			 */
			throw new ServletException(ex);
		}			
	}

	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		String requestURI = request.getRequestURI();
		if (requestURI == null) {
			return false;
		}

		for (String ignoreUrl : ignoreUrls) {
			if (requestURI.startsWith(ignoreUrl)) {
				LOGGER.info("ignore requestURI: " + requestURI);
				return true;
			}
		}

		return false;
	}
}
