/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import java.time.Clock;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

/**
 * The StopwatchExtension implements callback methods for the {@code @Stopwatch} annotation.
 */
class StopwatchExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

	private final Clock clock = Clock.systemUTC();
	private static final Namespace NAMESPACE = Namespace.create(StopwatchExtension.class);
	static final String STORE_KEY = "StopwatchExtension";

	@Override
	public void beforeTestExecution(ExtensionContext context) {
		storeNowAsLaunchTime(context);
	}

	@Override
	public void afterTestExecution(ExtensionContext context) {
		calculateAndReportElapsedTime(context);
	}

	private void storeNowAsLaunchTime(ExtensionContext context) {
		context.getStore(NAMESPACE).put(context.getUniqueId(), clock.instant().toEpochMilli());
	}

	private long loadLaunchTime(ExtensionContext context) {
		return context.getStore(NAMESPACE).get(context.getUniqueId(), long.class);
	}

	private void calculateAndReportElapsedTime(ExtensionContext context) {
		long launchTime = loadLaunchTime(context);
		long elapsedTime = clock.instant().toEpochMilli() - launchTime;

		String message = String.format("Execution of '%s' took [%d] ms.", context.getDisplayName(), elapsedTime);
		context.publishReportEntry(STORE_KEY, message);
	}

}
