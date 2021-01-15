/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junitpioneer.internal.PioneerAnnotationUtils;
import org.junitpioneer.internal.PioneerUtils;

class EnvironmentVariableExtension extends AbstractEntryBasedExtension<String, String> {

	// package visible to make accessible for tests
	static final AtomicBoolean REPORTED_WARNING = new AtomicBoolean(false);
	static final String WARNING_KEY = EnvironmentVariableExtension.class.getSimpleName();
	static final String WARNING_VALUE = "This extension uses reflection to mutate JDK-internal state, which is fragile. Check the Javadoc or documentation for more details.";

	@Override
	protected boolean isAnnotationPresent(ExtensionContext context) {
		return PioneerAnnotationUtils
				.isAnyRepeatableAnnotationPresent(context, ClearEnvironmentVariable.class,
					SetEnvironmentVariable.class);
	}

	@Override
	protected Set<String> entriesToClear(ExtensionContext context) {
		return PioneerAnnotationUtils
				.findClosestEnclosingRepeatableAnnotations(context, ClearEnvironmentVariable.class)
				.map(ClearEnvironmentVariable::key)
				.collect(PioneerUtils.distinctToSet());
	}

	@Override
	protected Map<String, String> entriesToSet(ExtensionContext context) {
		return PioneerAnnotationUtils
				.findClosestEnclosingRepeatableAnnotations(context, SetEnvironmentVariable.class)
				.collect(toMap(SetEnvironmentVariable::key, SetEnvironmentVariable::value));
	}

	@Override
	protected void reportWarning(ExtensionContext context) {
		boolean wasReported = REPORTED_WARNING.getAndSet(true);
		if (!wasReported)
			context.publishReportEntry(WARNING_KEY, WARNING_VALUE);
	}

	@Override
	protected void clearEntry(String key) {
		EnvironmentVariableUtils.clear(key);
	}

	@Override
	protected String getEntry(String key) {
		return System.getenv(key);
	}

	@Override
	protected void setEntry(String key, String value) {
		EnvironmentVariableUtils.set(key, value);
	}

}
