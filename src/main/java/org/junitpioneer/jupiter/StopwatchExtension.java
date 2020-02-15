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

import static java.lang.System.currentTimeMillis;
import static org.junitpioneer.jupiter.Utils.annotationPresentOnTestClass;

import java.time.Clock;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

/**
 * The StopwachtExtension implements callback methods for the {@code @Stopwatch} annotation.
 */
class StopwatchExtension
		implements BeforeAllCallback, BeforeTestExecutionCallback, AfterTestExecutionCallback, AfterAllCallback {
	private final Clock clock = Clock.systemUTC();
	private static final Namespace NAMESPACE = Namespace.create(StopwatchExtension.class);

	@Override
	public void beforeAll(ExtensionContext context) {
		if (annotationPresentOnTestClass(context, Stopwatch.class)) {
			storeNowAsLaunchTime(context, LaunchTimeKey.CLASS);
		}
	}

	@Override
	public void beforeTestExecution(ExtensionContext context) {
		if (annotationPresentOnTestClass(context, Stopwatch.class)) {
			storeNowAsLaunchTime(context, LaunchTimeKey.TEST);
		}
	}

	@Override
	public void afterTestExecution(ExtensionContext context) {
		if (annotationPresentOnTestClass(context, Stopwatch.class)) {
			long launchTime = loadLaunchTime(context, LaunchTimeKey.TEST);
			long elapsedTime = currentTimeMillis() - launchTime;
			report("Test", context, elapsedTime);
		}
	}

	@Override
	public void afterAll(ExtensionContext context) {
		if (annotationPresentOnTestClass(context, Stopwatch.class)) {
			long launchTime = loadLaunchTime(context, LaunchTimeKey.CLASS);
			long elapsedTime = currentTimeMillis() - launchTime;
			report("Class", context, elapsedTime);
		}
	}

	private void storeNowAsLaunchTime(ExtensionContext context, LaunchTimeKey key) {
		context.getStore(NAMESPACE).put(key, clock.instant().toEpochMilli());
	}

	private long loadLaunchTime(ExtensionContext context, LaunchTimeKey key) {
		return context.getStore(NAMESPACE).get(key, long.class);
	}

	private void report(String unit, ExtensionContext context, long elapsedTime) {
		String message = String.format("%s '%s' took %d ms.", unit, context.getDisplayName(), elapsedTime);

		System.out.println("Test-Method while developing: To see if extension is executed:" + message);
		context.publishReportEntry("stopwatch", message);
	}

	private enum LaunchTimeKey {
		CLASS, TEST
	}

}
