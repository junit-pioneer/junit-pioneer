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

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.extension.ExtensionContext;

class EnvironmentVariableExtension
		extends AbstractEntryBasedExtension<String, String, Map<String, String>,
				ClearEnvironmentVariable, SetEnvironmentVariable, RestoreEnvironmentVariables> {

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

	@Override
	protected Map<String, String> getAllCurrentEntries() {
		return System.getenv();
	}

	@Override
	protected void setAllCurrentEntries(final Map<String, String> restoreMe) {
		final Map<String, String> original = System.getenv();

		// Set all values, but only if different from actual value to avoid reflective set
		restoreMe.entrySet().parallelStream()
				.filter(e -> ! System.getenv(e.getKey()).equals(e.getValue()))
				.forEach(e -> setEntry(e.getKey(), e.getValue()));


		// Find entries to remove.
		// Cannot remove in stream b/c the stream is based on the collection that needs to be modified
		Set<String> entriesToClear = original.entrySet().parallelStream()
				.filter( e -> !restoreMe.containsKey(e.getKey()) )
				.map( e -> e.getKey())
				.collect(Collectors.toSet());

		entriesToClear.stream().forEach( k -> clearEntry(k) );
	}

}
