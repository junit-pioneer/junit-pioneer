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

import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.platform.commons.support.AnnotationSupport;

import java.lang.annotation.Annotation;
import java.time.Clock;
import java.util.*;
import java.util.stream.Stream;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.setOut;
import static java.util.stream.Collectors.toMap;
import static org.junit.platform.commons.util.AnnotationUtils.isAnnotated;
import static org.junitpioneer.jupiter.Utils.annotationPresentOnTestClass;
import static org.junitpioneer.jupiter.Utils.annotationPresentOnTestMethod;

/*
TODO:
- Extension is not Executed on Classlevel
- How to access context to get value of report
 */

class StopwatchExtension implements BeforeAllCallback, BeforeTestExecutionCallback,
		AfterTestExecutionCallback, AfterAllCallback {
	private final Clock clock = Clock.systemUTC();
	private static final Namespace NAMESPACE = Namespace.create(StopwatchExtension.class);

	// The Utils annotationPresentOnTestMethod does not recognizes Class-Annotations
	private static boolean shouldBeBenchmarked(ExtensionContext context) {
		return context.getElement()
				.map(el -> isAnnotated(el, Stopwatch.class))
				.orElse(false);
	}

	@Override
	public void beforeAll(ExtensionContext context)  {
//		if(annotationPresentOnTestMethod(context, Stopwatch.class) || annotationPresentOnTestClass(context, Stopwatch.class)) {
//			storeNowAsLaunchTime(context, LaunchTimeKey.CLASS);
//		}
		if(shouldBeBenchmarked(context)) {
			storeNowAsLaunchTime(context, LaunchTimeKey.CLASS);
		}
	}

	@Override
	public void beforeTestExecution(ExtensionContext context) {
//		if(annotationPresentOnTestMethod(context, Stopwatch.class)) {
//			storeNowAsLaunchTime(context, LaunchTimeKey.TEST);
//		}


		if(shouldBeBenchmarked(context)) {
			storeNowAsLaunchTime(context, LaunchTimeKey.TEST);
		}
	}


	@Override
	public void afterTestExecution(ExtensionContext context) {
//		if(!annotationPresentOnTestMethod(context, Stopwatch.class)) {
//			return;
//		}
//
//		long launchTime = loadLaunchTime(context, LaunchTimeKey.TEST);
//		long elapsedTime = currentTimeMillis() - launchTime;
//		report("Test", context, elapsedTime);

		if(shouldBeBenchmarked(context)) {
			long launchTime = loadLaunchTime(context, LaunchTimeKey.TEST);
			long elapsedTime = currentTimeMillis() - launchTime;
			report("Test", context, elapsedTime);
		}
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
//		if(!annotationPresentOnTestMethod(context, Stopwatch.class)) {
//			return;
//		}
//
//		long launchTime = loadLaunchTime(context, LaunchTimeKey.CLASS);
//		long elapsedTime = clock.instant().toEpochMilli() - launchTime;
//		report("Test container", context, elapsedTime);

		if(shouldBeBenchmarked(context)) {
			long launchTime = loadLaunchTime(context, LaunchTimeKey.CLASS);
			long elapsedTime = currentTimeMillis() - launchTime;
			report("Test", context, elapsedTime);
		}
	}

	private void storeNowAsLaunchTime(
			ExtensionContext context, LaunchTimeKey key) {
		context.getStore(NAMESPACE).put(key, clock.instant().toEpochMilli());
	}

	private long loadLaunchTime(
			ExtensionContext context, LaunchTimeKey key) {
		return context.getStore(NAMESPACE).get(key, long.class);
	}

	private void report(
			String unit, ExtensionContext context, long elapsedTime) {
		String message = String.format(
				"%s '%s' took %d ms.",
				unit, context.getDisplayName(), elapsedTime);


		System.out.println("To see if extension is executed:" + message);
		context.publishReportEntry("stopwatch", message);
	}

	private enum LaunchTimeKey {
		CLASS, TEST
	}

}
