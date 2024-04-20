/*
 * Copyright 2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

import static java.lang.String.format;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;
import static org.junitpioneer.internal.PioneerAnnotationUtils.findClosestEnclosingAnnotation;

class DisabledIfNotReachableExtension implements ExecutionCondition {

	private static final Namespace NAMESPACE = Namespace.create(DisabledIfNotReachableExtension.class);
	private static final String DISABLED_KEY = "DISABLED_KEY";
	private static final String DISABLED_VALUE = "";

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		var optAnnotation = findClosestEnclosingAnnotation(context, DisabledIfNotReachable.class);

		if (optAnnotation.isEmpty()) {
			return enabled("No @DisabledIfNotReachable annotation found.");
		}

		var config = readConfigurationFromAnnotation(optAnnotation.get());

	}

	/**
	 * Pings an HTTP URL. This effectively sends a HEAD request and returns
	 * {@code true} if the response code is in the 200-399 range.
	 *
	 * Based on <a href="https://stackoverflow.com/users/157882/balusc">BalusC</a>'s answer on StackOverflow to
	 * <a href="https://stackoverflow.com/a/3584332/2525313">Preferred Java way to ping an HTTP URL for availability</a>
	 * but with JDK 11 HTTP-Client.
	 *
	 * @param config
	 *  Configuration, including the url and a timeout value, based on annotation value
	 * @param context
	 * 	Extension context to get the unique ID of the test to be executed
	 *
	 *
	 * @return {@code true} if the given HTTP URL has returned response
	 * code 200-399 on a HEAD request within the given timeout, otherwise
	 * {@code false}.
	 */
	private static ConditionEvaluationResult pingUrl(DisabledIfNotReachableConfiguration config, ExtensionContext context) {

		String url = config.getUrl();
		int timeout = config.getTimeOut();

		https: //openjdk.org/groups/net/httpclient/intro.html

		boolean reachable = false;

		if (reachable) {
			return enabled(format("%s is enabled because %s is reachable", context.getUniqueId(), config.getUrl()));
		} else {
			return disabled(format("%s is disabled because %s could not be reached in %dms", context.getUniqueId(),
					config.getUrl(), config.getTimeOut()));
		}


//	 * @param config
//				* 		The HTTP URL to be pinged.
//				* @param timeoutMillis
//				* 		The timeout in millis for both the connection timeout and the
//	 * 		response read timeout. Note that the total timeout is effectively
//				* 		two times the given timeout.
		//		// Otherwise an exception may be thrown on invalid SSL certificates
		//		String httpUrl = url.replaceFirst("^https", "http");
		//		try {
		//			HttpURLConnection connection = (HttpURLConnection) new URL(httpUrl).openConnection();
		//			connection.setConnectTimeout(timeoutMillis);
		//			connection.setReadTimeout(timeoutMillis);
		//			connection.setRequestMethod("HEAD");
		//			int responseCode = connection.getResponseCode();
		//			return (200 <= responseCode && responseCode <= 399);
		//		} catch (IOException exception) {
		//			return false;
		//		}
	}

	private DisabledIfNotReachableConfiguration readConfigurationFromAnnotation(DisabledIfNotReachable annotation) {
		return new DisabledIfNotReachableConfiguration(annotation.url(), annotation.timeoutMillis());
	}

	/**
	 * Simple Class that holds the configuration for the URL-check of the {@code DisabledIfNotReachableExtension}.
	 */
	private class DisabledIfNotReachableConfiguration {

		// Change to record when after migrating to Java 16+
		private final String url;
		private final int timeOut;

		public DisabledIfNotReachableConfiguration(String url, int timeOut) {
			this.url = url;
			this.timeOut = timeOut;
		}

		public int getTimeOut() {
			return timeOut;
		}

		public String getUrl() {
			return url;
		}

	}

}
