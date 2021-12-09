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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.junit.jupiter.api.extension.ExtensionContext;

class EnvironmentVariableExtension
		extends AbstractEntryBasedExtension<String, String, ClearEnvironmentVariable, SetEnvironmentVariable> {

	// package visible to make accessible for tests
	static final AtomicBoolean REPORTED_WARNING = new AtomicBoolean(false);
	static final String WARNING_KEY = EnvironmentVariableExtension.class.getSimpleName();
	static final String WARNING_VALUE = "This extension uses reflection to mutate JDK-internal state, which is fragile. Check the Javadoc or documentation for more details.";

	@Override
	protected Function<ClearEnvironmentVariable, String> clearKeyMapper() {
		return ClearEnvironmentVariable::key;
	}

	@Override
	protected Function<SetEnvironmentVariable, String> setKeyMapper() {
		return SetEnvironmentVariable::key;
	}

	@Override
	protected Function<SetEnvironmentVariable, String> setValueMapper() {
		return SetEnvironmentVariable::value;
	}

	@Override
	protected void reportWarning(ExtensionContext context) {
		boolean wasReported = REPORTED_WARNING.getAndSet(true);
		if (wasReported)
			return;

		// Log as report entry and to System.out - check docs for reasons, but why System.out?
		// Because report entries lack tool support and are easily lost and System.err is
		// too invasive (particularly since, with good configuration, the module system won't
		// print a warning and hence it's only Pioneer polluting System.err - not good).
		// System.out seemed like a good compromise.
		context.publishReportEntry(WARNING_KEY, WARNING_VALUE);
		System.out.println(WARNING_KEY + ": " + WARNING_VALUE); //NOSONAR
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
