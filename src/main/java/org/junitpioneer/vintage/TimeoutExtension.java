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
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

/**
 * This extension implements the timeout behavior of {@link Test @Test}, where a test is failed if it takes longer to finish
 * than the specified time.
 *
 * <p>Note that this is different from JUnit 4's {@code @Test} parameter, which would abandon the test if it ran to
 * long and continue with the remainder of the suite. As Jupiter's extension API is currently not powerful enough
 * to interact with its threading model, this could not be implemented.
 */
class TimeoutExtension implements InvocationInterceptor {

	static final String TEST_RAN_TOO_LONG = "Test '%s' was supposed to run no longer than %d ms.";

	@Override
	public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
			ExtensionContext extensionContext) throws Throwable {
		Optional<Long> optionalTimeout = annotatedTimeout(extensionContext);
		if (optionalTimeout.isPresent())
			proceedWithTimeout(invocation, extensionContext, optionalTimeout.get());
		else
			invocation.proceed();
	}

	private void proceedWithTimeout(Invocation<Void> invocation, ExtensionContext extensionContext, long timeout) {
		if (timeout < 0)
			throw new ExtensionConfigurationException("Timeout for vintage @Test must be positive.");

		assertTimeoutPreemptively(Duration.ofMillis(timeout), invocation::proceed,
			format(TEST_RAN_TOO_LONG, extensionContext.getDisplayName(), timeout));
	}

	private Optional<Long> annotatedTimeout(ExtensionContext context) {
		return findAnnotation(context.getElement(), Test.class).map(Test::timeout).filter(timeout -> timeout != 0L);
	}

}
