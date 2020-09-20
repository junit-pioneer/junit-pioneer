/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.vintage;

import static java.lang.String.format;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

import java.util.Optional;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.opentest4j.AssertionFailedError;

/**
 * This extension implements the expected exception behavior of {@link Test @Test}, where a test only passes if it throws
 * an exception of the specified type.
 */
class ExpectedExceptionExtension implements TestExecutionExceptionHandler, AfterTestExecutionCallback {

	/*
	 * This extension implements the exception handler callback to compare the thrown exception
	 * to what was expected. The after test callback (which is called later) builds on
	 * the results of that check and fails the test if an exception was expected but not thrown.
	 */

	static final String EXPECTED_EXCEPTION_WAS_NOT_THROWN = "Expected exception %s was not thrown.";

	private static final Namespace NAMESPACE = Namespace.create(ExpectedExceptionExtension.class);
	private static final String KEY = "ExceptionWasThrown";

	@Override
	public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
		boolean throwableMatchesExpectedException = expectedException(context)
				.filter(expected -> expected.isInstance(throwable))
				.isPresent();

		// In the `afterTestExecution` callback we have to pass or fail the test
		// depending on whether the exception was thrown or not.
		// To do that we need to register whether the exception was thrown
		// (NOTE that if no exception was thrown, NOTHING is registered).
		if (throwableMatchesExpectedException) {
			storeExceptionStatus(context, EXCEPTION.WAS_THROWN_AS_EXPECTED);
		} else {
			// this extension is not in charge of the throwable, so we need to rethrow
			storeExceptionStatus(context, EXCEPTION.WAS_THROWN_NOT_AS_EXPECTED);
			throw throwable;
		}
	}

	@Override
	public void afterTestExecution(ExtensionContext context) throws Exception {
		switch (loadExceptionStatus(context)) {
			case WAS_NOT_THROWN:
				expectedException(context)
						.map(expected -> new AssertionFailedError(format(EXPECTED_EXCEPTION_WAS_NOT_THROWN, expected)))
						.ifPresent(error -> {
							throw error;
						});
			case WAS_THROWN_AS_EXPECTED:
				// the exception was thrown as expected so there is nothing to do
				break;
			case WAS_THROWN_NOT_AS_EXPECTED:
				// An exception was thrown but of the wrong type.
				// It was rethrown in `handleTestExecutionException` so there is nothing to do here
				break;
			default:
				// This default block can't be reached without the EXCEPTION enum is changed via reflection.
				// So there is no test case for it.
				throw new IllegalArgumentException("Invalid exceptionStatus");
		}
	}

	private static Optional<? extends Class<? extends Throwable>> expectedException(ExtensionContext context) {
		return findAnnotation(context.getElement(), Test.class)
				.map(Test::expected)
				.filter(exceptionType -> exceptionType != Test.None.class);
	}

	private static void storeExceptionStatus(ExtensionContext context, EXCEPTION thrown) {
		context.getStore(NAMESPACE).put(KEY, thrown);
	}

	private static EXCEPTION loadExceptionStatus(ExtensionContext context) {
		EXCEPTION thrown = context.getStore(NAMESPACE).get(KEY, EXCEPTION.class);
		if (thrown == null) {
			return EXCEPTION.WAS_NOT_THROWN;
		} else {
			return thrown;
		}
	}

	private enum EXCEPTION {
		WAS_NOT_THROWN, WAS_THROWN_AS_EXPECTED, WAS_THROWN_NOT_AS_EXPECTED,
	}

}
