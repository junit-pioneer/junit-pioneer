/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit$pioneer.vintage;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

import java.util.Optional;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

/**
 * This extension implements the timeout behavior of {@link Test @Test}, where a test is failed if it takes longer to finish
 * than the specified time.
 *
 * <p>Note that this is different from JUnit 4's {@code @Test} parameter, which would abandon the test if it ran to
 * long and continue with the remainder of the suite. As Jupiter's extension API is currently not powerful enough
 * to interact with its threading model, this could not be implemented.
 */
class TimeoutExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

	static final String TEST_RAN_TOO_LONG = "Test '%s' was supposed to run no longer than %d ms but ran %d ms.";

	private static final Namespace NAMESPACE = Namespace.create("io", "Timeout");
	private static final String LAUNCH_TIME_KEY = "LaunchTime";

	@Override
	public void beforeTestExecution(ExtensionContext context) {
		storeNowAsLaunchTime(context);
	}

	@Override
	public void afterTestExecution(ExtensionContext context) {
		annotatedTimeout(context).ifPresent(timeout -> failTestIfRanTooLong(context, timeout));
	}

	private void failTestIfRanTooLong(ExtensionContext context, Long timeout) {
		long launchTime = loadLaunchTime(context);
		long elapsedTime = currentTimeMillis() - launchTime;

		if (elapsedTime > timeout) {
			String message = format(TEST_RAN_TOO_LONG, context.getDisplayName(), timeout, elapsedTime);
			throw new AssertionError(message);
		}
	}

	private Optional<Long> annotatedTimeout(ExtensionContext context) {
		return findAnnotation(context.getElement(), Test.class).map(Test::timeout).filter(timeout -> timeout != 0L);
	}

	private static void storeNowAsLaunchTime(ExtensionContext context) {
		context.getStore(NAMESPACE).put(LAUNCH_TIME_KEY, currentTimeMillis());
	}

	private static long loadLaunchTime(ExtensionContext context) {
		return context.getStore(NAMESPACE).get(LAUNCH_TIME_KEY, long.class);
	}

}
