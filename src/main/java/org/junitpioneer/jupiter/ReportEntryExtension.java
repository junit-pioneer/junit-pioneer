/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static java.lang.String.format;
import static org.junitpioneer.jupiter.ReportEntry.PublishCondition.ALWAYS;
import static org.junitpioneer.jupiter.ReportEntry.PublishCondition.ON_ABORTED;
import static org.junitpioneer.jupiter.ReportEntry.PublishCondition.ON_FAILURE;
import static org.junitpioneer.jupiter.ReportEntry.PublishCondition.ON_SUCCESS;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.TestWatcher;

class ReportEntryExtension implements TestWatcher, BeforeEachCallback, InvocationInterceptor {

	private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace
			.create(ReportEntryExtension.class);

	private static final String KEY = "ReportEntry";

	@Override
	public void beforeEach(ExtensionContext context) {
		findAnnotations(context).forEach(entry -> verifyReportEntry(context, entry));
	}

	private Stream<ReportEntry> findAnnotations(ExtensionContext context) {
		return PioneerAnnotationUtils.findAllEnclosingRepeatableAnnotations(context, ReportEntry.class);
	}

	private static void verifyReportEntry(ExtensionContext context, ReportEntry entry) {
		verifyParameterCount(context, entry);
		verifyKeyValueAreNotBlank(entry);
		verifyKeyNotParameterized(entry);
	}

	private static void verifyParameterCount(ExtensionContext context, ReportEntry entry) {
		if (hasTestParameterVariables(entry.value())) {
			int highest = getHighestNumberedParameter(entry);
			if (context.getRequiredTestMethod().getParameterCount() <= highest) {
				String message = "Report entry contains unresolved variable(s): { key=\"%s\" value=\"%s\" }";
				throw new ExtensionConfigurationException(format(message, entry.key(), entry.value()));
			}
		}
	}

	private static int getHighestNumberedParameter(ReportEntry entry) {
		int highest = 0;
		Matcher matcher = Pattern.compile("\\{[0-9]+}").matcher(entry.value());
		while (matcher.find())
			highest = Math.max(getVariableNumber(entry, matcher), highest);
		return highest;
	}

	private static int getVariableNumber(ReportEntry entry, Matcher matcher) {
		return Integer.parseInt(entry.value().substring(matcher.start() + 1, matcher.end() - 1));
	}

	private static void verifyKeyValueAreNotBlank(ReportEntry entry) {
		if (entry.key().isEmpty() || entry.value().isEmpty()) {
			String message = "Report entries can't have blank key or value: { key=\"%s\", value=\"%s\" }";
			throw new ExtensionConfigurationException(format(message, entry.key(), entry.value()));
		}
	}

	private static void verifyKeyNotParameterized(ReportEntry entry) {
		if (hasTestParameterVariables(entry.key())) {
			String message = "Report entry can not have variables in the key: { key=\"%s\" value=\"%s\" }";
			throw new ExtensionConfigurationException(format(message, entry.key(), entry.value()));
		}
	}

	@Override
	public void testDisabled(ExtensionContext context, Optional<String> reason) {
		// if the test is disabled, we consider the annotation disabled too and don't publish anything
	}

	@Override
	public void testSuccessful(ExtensionContext context) {
		publishOnConditions(context, ALWAYS, ON_SUCCESS);
	}

	@Override
	public void testAborted(ExtensionContext context, Throwable cause) {
		publishOnConditions(context, ALWAYS, ON_ABORTED);
	}

	@Override
	public void testFailed(ExtensionContext context, Throwable cause) {
		// we assume that if a message should be logged for failed tests,
		// then because the test ran and failed - not because it was
		// configured incorrectly; hence we don't log in this case
		if (!(cause instanceof ExtensionConfigurationException))
			publishOnConditions(context, ALWAYS, ON_FAILURE);
	}

	private void publishOnConditions(ExtensionContext context, ReportEntry.PublishCondition... conditions) {
		findAnnotations(context)
				.filter(entry -> Arrays.asList(conditions).contains(entry.when()))
				.forEach(entry -> context.publishReportEntry(entry.key(), parseVariables(entry.value(), context)));
	}

	private String parseVariables(String value, ExtensionContext context) {
		if (!hasTestParameterVariables(value))
			return value;

		String parsed = value;
		List<?> list = context.getStore(NAMESPACE).get(KEY, List.class);
		for (int i = 0; i < list.size(); i++) {
			parsed = parsed.replaceAll("\\{" + i + "}", list.get(i).toString());
		}

		return parsed;
	}

	private static boolean hasTestParameterVariables(String value) {
		return value.matches(".*\\{[0-9]+}.*");
	}

	@Override
	public void interceptTestTemplateMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		extensionContext.getStore(NAMESPACE).put(KEY, invocationContext.getArguments());
		invocation.proceed();
	}

}
