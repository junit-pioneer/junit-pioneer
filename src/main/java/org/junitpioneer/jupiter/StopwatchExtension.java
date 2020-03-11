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

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junitpioneer.jupiter.enumerations.TestunitEnum;

import java.time.Clock;

import static java.lang.System.currentTimeMillis;
import static org.junitpioneer.jupiter.Utils.annotationPresentOnTestClass;

/**
 * The StopwatchExtension implements callback methods for the {@code @Stopwatch} annotation.
 */
class StopwatchExtension
		implements BeforeAllCallback, BeforeTestExecutionCallback, AfterTestExecutionCallback, AfterAllCallback {
	private final Clock clock = Clock.systemUTC();
	private static final Namespace NAMESPACE = Namespace.create(StopwatchExtension.class);

	@Override
	public void beforeAll(ExtensionContext context) {
		if (annotationPresentOnTestClass(context, Stopwatch.class)) {
			storeNowAsLaunchTime(context, TestunitEnum.CLASS);
		}
	}

	@Override
	public void beforeTestExecution(ExtensionContext context) {
		if (annotationPresentOnTestClass(context, Stopwatch.class)) {
			storeNowAsLaunchTime(context, TestunitEnum.TEST);
		}
	}

	@Override
	public void afterTestExecution(ExtensionContext context) {
		if (annotationPresentOnTestClass(context, Stopwatch.class)) {
			calculateAndReportElapsedTime(context, TestunitEnum.TEST);
		}
	}

	@Override
	public void afterAll(ExtensionContext context) {
		if (annotationPresentOnTestClass(context, Stopwatch.class)) {
			calculateAndReportElapsedTime(context, TestunitEnum.CLASS);
		}
	}

	/**
	 * Stores the current time for the given testunit in the execution context.
	 *
	 * @param context Extension context of the class
	 * @param unit Testobject for which the time should be stored
	 */
	void storeNowAsLaunchTime(ExtensionContext context, TestunitEnum unit) {
		context.getStore(NAMESPACE).put(unit, clock.instant().toEpochMilli());
	}

	/**
	 * Loads the stored time for the given testunit from the execution context.
	 *
	 * @param context Extension context of the class
	 * @param unit Testobject for which the time should be stored
	 */
	long loadLaunchTime(ExtensionContext context, TestunitEnum unit) {
		return context.getStore(NAMESPACE).get(unit, long.class);
	}

	/**
	 * Calculates the elapsed time for the testunit and publishs it to the execution context.
	 *
	 * @param context Extension context of the class
	 * @param unit Testunit for which the time should be calculated and published
	 */
	void calculateAndReportElapsedTime(ExtensionContext context, TestunitEnum unit) {
		long launchTime = loadLaunchTime(context, unit);
		long elapsedTime = currentTimeMillis() - launchTime;

		String message = String.format("%s '%s' took %d ms.", unit.name(), context.getDisplayName(), elapsedTime);
		context.publishReportEntry("stopwatch", message);
	}

}
