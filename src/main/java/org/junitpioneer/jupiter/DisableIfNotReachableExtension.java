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

import static java.lang.String.format;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;
import static org.junitpioneer.internal.PioneerAnnotationUtils.findClosestEnclosingAnnotation;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junitpioneer.internal.PioneerPreconditions;

class DisableIfNotReachableExtension implements ExecutionCondition {

	private static final Namespace NAMESPACE = Namespace.create(DisableIfNotReachableExtension.class);
	private static final String HTTP_CLIENT_STORE_KEY = "DISABLED_KEY";

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		var optAnnotation = findClosestEnclosingAnnotation(context, DisableIfNotReachable.class);

		if (optAnnotation.isEmpty()) {
			return enabled("No @DisableIfNotReachable annotation found.");
		}

		var config = readConfigurationFromAnnotation(optAnnotation.get());

		return pingUrl(config, context);
	}

	/**
	 * Pings an HTTP URL. This effectively sends a GET request and returns
	 * {@code true} if the response code is in the 200-399 range.
	 * <p>
	 * Based on <a href="https://stackoverflow.com/users/157882/balusc">BalusC</a>'s answer on StackOverflow to
	 * <a href="https://stackoverflow.com/a/3584332/2525313">Preferred Java way to ping an HTTP URL for availability</a>
	 * but with <a href="https://openjdk.org/groups/net/httpclient/intro.html">JDK 11 HTTP-Client</a>.
	 *
	 * @param config
	 *  Configuration, including the url and a timeout value, based on annotation value
	 * @param context
	 * 	Extension context to get the unique ID of the test to be executed
	 *
	 * @return {@code true} if the given HTTP URL has returned response
	 * code 200-399 on a GET request within the given timeout, otherwise
	 * {@code false}.
	 */
	private static ConditionEvaluationResult pingUrl(DisabledIfNotReachableConfiguration config,
			ExtensionContext context) {
		HttpClient client = context
				.getStore(NAMESPACE)
				.computeIfAbsent(HTTP_CLIENT_STORE_KEY, __ -> createHttpClient(), HttpClient.class);

		boolean reachable = false;

		try {
			HttpRequest request = HttpRequest
					.newBuilder()
					.uri(URI.create(config.url))
					.timeout(Duration.ofMillis(config.timeout))
					.GET()
					.build();

			int responseCode = client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();

			// We consider the target reachable if we get an HTTP response code which does not indicate an error.
			reachable = (200 <= responseCode && responseCode <= 399);
		}
		catch (IOException e) {
			return disabled(format("%s is disabled because trying to ping %s failed with exception: %s",
				context.getUniqueId(), config.url, e.getMessage()));
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return disabled(format("%s is disabled because trying to ping %s failed with exception: %s",
				context.getUniqueId(), config.url, e.getMessage()));
		}

		if (reachable) {
			return enabled(format("%s is enabled because %s is reachable", context.getUniqueId(), config.url));
		} else {
			return disabled(format("%s is disabled because %s could not be reached in %dms", context.getUniqueId(),
				config.url, config.timeout));
		}
	}

	private static HttpClient createHttpClient() {
		return HttpClient
				.newBuilder()
				.version(HttpClient.Version.HTTP_2)
				.followRedirects(HttpClient.Redirect.NORMAL)
				.build();
	}

	private DisabledIfNotReachableConfiguration readConfigurationFromAnnotation(DisableIfNotReachable annotation) {
		if (annotation.url().isBlank()) {
			throw new ExtensionConfigurationException("URL can not be empty");
		}
		try {
			URI url = URI.create(annotation.url());
			if (url.getScheme() == null || !url.getScheme().startsWith("http")) {
				throw new ExtensionConfigurationException(format("Scheme for URL %s must be http or https", annotation.url()));
			}
		}
		catch (IllegalArgumentException e) {
			throw new ExtensionConfigurationException(format("URL %s is invalid", annotation.url()), e);
		}

		if (annotation.timeoutMillis() <= 0) {
			throw new ExtensionConfigurationException("Timeout must be greater than zero");
		}

		return new DisabledIfNotReachableConfiguration(annotation.url(), annotation.timeoutMillis());
	}

	/**
	 * Simple Class that holds the configuration for the URL-check of the {@code DisabledIfNotReachableExtension}.
	 */
	private record DisabledIfNotReachableConfiguration(String url, int timeout) {
	}

}
