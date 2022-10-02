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

class NotWorkingExtension implements Extension, InvocationInterceptor {

	/**
	 * No-arg constructor for JUnit to be able to create an instance.
	 */
	public NotWorkingExtension() {
	}

	private static NotWorking getNotWorkingAnnotation(ExtensionContext context) {
		return AnnotationSupport
				.findAnnotation(context.getRequiredTestMethod(), NotWorking.class)
				.orElseThrow(() -> new IllegalStateException("@NotWorking is missing."));

	}

	/**
	 * Returns whether the exception should be preserved and reported as is instead
	 * of considering it an expected 'not working' exception.
	 *
	 * <p>This method is used for exceptions which abort test execution and should
	 * have higher precedence than aborted exceptions thrown by this extension.
	 */
	private static boolean shouldPreserveException(Throwable t) {
		// Note: Ideally would use the same logic JUnit uses to determine if exception is aborting
		// execution, see its class OpenTest4JAndJUnit4AwareThrowableCollector
		return TestAbortedException.class.isInstance(t);
	}

	private static <T> T invokeAndInvertResult(Invocation<T> invocation, ExtensionContext extensionContext)
			throws Throwable {
		try {
			invocation.proceed();
			// if no exception was thrown fall through and call fail(...) eventually
		}
		catch (Throwable t) {
			if (shouldPreserveException(t)) {
				throw t;
			}

			NotWorking annotation = getNotWorkingAnnotation(extensionContext);

			String message = annotation.value();
			if (message.isEmpty()) {
				message = "Test marked as 'not working' failed as expected";
			}

			throw new TestAbortedException(message, t);
		}

		return fail("Test marked as 'not working' succeeded; remove @NotWorking from it");
	}

	@Override
	public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
			ExtensionContext extensionContext) throws Throwable {
		invokeAndInvertResult(invocation, extensionContext);
	}

}
