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

import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Method;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.opentest4j.TestAbortedException;

class ExpectedToFailExtension implements Extension, InvocationInterceptor {

	@Override
	public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
			ExtensionContext extensionContext) throws Throwable {
		invokeAndInvertResult(invocation, extensionContext);
	}

	private static <T> T invokeAndInvertResult(Invocation<T> invocation, ExtensionContext extensionContext)
			throws Throwable {
		try {
			invocation.proceed();
			// at this point, the invocation succeeded, so we'd want to call `fail(...)`,
			// but that would get handled by the following `catch` and so it's easier
			// to instead fall through to a `fail(...)` after the `catch` block
		}
		catch (Throwable t) {
			if (shouldPreserveException(t)) {
				throw t;
			}

			String message = getExpectedToFailAnnotation(extensionContext).value();
			if (message.isEmpty()) {
				message = "Test marked as temporarily 'expected to fail' failed as expected";
			}

			throw new TestAbortedException(message, t);
		}

		return fail("Test marked as 'expected to fail' succeeded; remove @ExpectedToFail from it");
	}

	/**
	 * Returns whether the exception should be preserved and reported as is instead
	 * of considering it an 'expected to fail' exception.
	 *
	 * <p>This method is used for exceptions that abort test execution and should
	 * have higher precedence than aborted exceptions thrown by this extension.
	 */
	private static boolean shouldPreserveException(Throwable t) {
		// Note: Ideally would use the same logic JUnit uses to determine if exception is aborting
		// execution, see its class OpenTest4JAndJUnit4AwareThrowableCollector
		return TestAbortedException.class.isInstance(t);
	}

	private static ExpectedToFail getExpectedToFailAnnotation(ExtensionContext context) {
		return AnnotationSupport
				.findAnnotation(context.getRequiredTestMethod(), ExpectedToFail.class)
				.orElseThrow(() -> new IllegalStateException("@ExpectedToFail is missing."));

	}

}
