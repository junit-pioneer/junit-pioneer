/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static java.lang.String.format;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.jupiter.api.extension.TestWatcher;
import org.junitpioneer.internal.PioneerAnnotationUtils;
import org.junitpioneer.internal.TestNameFormatter;
import org.opentest4j.TestAbortedException;

class FlakyExtension implements TestExecutionExceptionHandler, TestTemplateInvocationContextProvider,
		ExecutionCondition, TestWatcher {

	private static final Namespace NAMESPACE = Namespace.create(FlakyExtension.class);

	@Override
	public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
		var root = context.getRoot();
		if (root.getStore(NAMESPACE).get(repeatId(context)) == null) {
			// repeat id is only present in the store for the test template
			root.getStore(NAMESPACE).put(uniqueId(context), Status.TEST_FAILED);
			throw new TestAbortedException("The test has failed and will be retried.", throwable);
		} else {
			// The test method being present means this is the test template
			int i = root.getStore(NAMESPACE).get(repeatId(context), int.class);
			if (i > 0) {
				root.getStore(NAMESPACE).put(repeatId(context), i - 1);
				throw new TestAbortedException("The test has failed and will be retried", throwable);
			}
			root.getStore(NAMESPACE).remove(repeatId(context));
			root.getStore(NAMESPACE).put(uniqueId(context), Status.TEST_TEMPLATE_FAILED);
			throw throwable;
		}
	}

	@Override
	public void testSuccessful(ExtensionContext context) {
		context.getRoot().getStore(NAMESPACE).put(uniqueId(context), Status.TEST_SUCCESSFUL);
	}

	@Override
	public boolean supportsTestTemplate(ExtensionContext context) {
		return PioneerAnnotationUtils.isAnnotationPresent(context, Flaky.class);
	}

	@Override
	public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
		Flaky flaky = findAnnotation(context.getTestMethod(), Flaky.class)
				.orElseThrow(() -> new IllegalStateException(
					"Flaky extension was invoked but @Flaky annotation is not present."));
		final var formatter = new TestNameFormatter(flaky.name(), context.getDisplayName(), Flaky.class);
		int value = flaky.value();
		if (value <= 1) {
			String signature = context.getRequiredTestClass().getSimpleName() + "#"
					+ context.getRequiredTestMethod().getName();
			throw new ExtensionConfigurationException(String
					.format("%s#value() must be greater than 1 (was %s) on %s", Flaky.class.getSimpleName(), value,
						signature));
		}
		if (context.getRoot().getStore(NAMESPACE).get(uniqueId(context)) != null) {
			// normal test ran, one less test template
			value -= 1;
		}
		context.getRoot().getStore(NAMESPACE).put(repeatId(context), value - 1);
		return Stream.generate(() -> new FlakyTestInvocationContext(formatter)).limit(value).map(Function.identity());
	}

	// This is invoked BEFORE test templates get created!
	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		var root = context.getRoot();
		var testRunResult = root.getStore(NAMESPACE).get(uniqueId(context));
		if (testRunResult == Status.TEST_SUCCESSFUL)
			return ConditionEvaluationResult.disabled("There was a successful test, subsequent runs can be disabled.");
		if (testRunResult == Status.TEST_TEMPLATE_FAILED)
			return ConditionEvaluationResult.disabled("Test template ran and failed, test is skipped.");
		return ConditionEvaluationResult.enabled("Test can run.");
	}

	private static String uniqueId(ExtensionContext context) {
		return context.getRoot().getUniqueId() + format("/[method:%s()]", context.getRequiredTestMethod().getName());
	}

	private static String repeatId(ExtensionContext context) {
		return uniqueId(context) + "$repeats";
	}

	private enum Status {
		TEST_FAILED, TEST_TEMPLATE_FAILED, TEST_SUCCESSFUL // or test template successful
	}

}
