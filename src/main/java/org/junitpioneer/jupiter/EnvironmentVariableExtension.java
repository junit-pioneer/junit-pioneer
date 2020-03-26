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

import static java.util.stream.Collectors.toMap;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.platform.commons.support.AnnotationSupport;

class EnvironmentVariableExtension
		implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback, AfterEachCallback {

	private static final Namespace NAMESPACE = Namespace.create(EnvironmentVariableExtension.class);
	private static final String BACKUP = "Backup";

	private static final AtomicBoolean REPORTED_WARNING = new AtomicBoolean(false);
	// package visible to make accessible for tests
	static final String WARNING_KEY = EnvironmentVariableExtension.class.getSimpleName();
	static final String WARNING_VALUE = "This extension uses reflection to mutate JDK-internal state, which is fragile. Check the Javadoc or documentation for more details.";

	@Override
	public void beforeAll(ExtensionContext context) {
		handleEnvironmentVariables(context);
	}

	@Override
	public void beforeEach(ExtensionContext context) {
		boolean present = Utils
				.annotationPresentOnTestMethod(context, ClearEnvironmentVariable.class, ClearEnvironmentVariables.class,
					SetEnvironmentVariable.class, SetEnvironmentVariables.class);
		if (present) {
			handleEnvironmentVariables(context);
		}
	}

	private void handleEnvironmentVariables(ExtensionContext context) {
		Set<String> variablesToClear;
		Map<String, String> variablesToSet;
		try {
			variablesToClear = findRepeatableAnnotations(context, ClearEnvironmentVariable.class)
					.stream()
					.map(ClearEnvironmentVariable::key)
					.collect(Utils.distinctToSet());
			variablesToSet = findRepeatableAnnotations(context, SetEnvironmentVariable.class)
					.stream()
					.collect(toMap(SetEnvironmentVariable::key, SetEnvironmentVariable::value));
			preventClearAndSetSameEnvironmentVariables(variablesToClear, variablesToSet.keySet());
		}
		catch (IllegalStateException ex) {
			throw new ExtensionConfigurationException("Don't clear/set the same environment variable more than once.",
				ex);
		}

		storeOriginalEnvironmentVariables(context, variablesToClear, variablesToSet.keySet());
		reportWarning(context);
		EnvironmentVariableUtils.clear(variablesToClear);
		EnvironmentVariableUtils.set(variablesToSet);
	}

	private <A extends Annotation> List<A> findRepeatableAnnotations(ExtensionContext context,
			Class<A> annotationType) {
		return context
				.getElement()
				.map(element -> AnnotationSupport.findRepeatableAnnotations(element, annotationType))
				.orElseGet(Collections::emptyList);
	}

	private void preventClearAndSetSameEnvironmentVariables(Collection<String> variablesToClear,
			Collection<String> variablesToSet) {
		variablesToClear
				.stream()
				.filter(variablesToSet::contains)
				.reduce((k0, k1) -> k0 + ", " + k1)
				.ifPresent(duplicateKeys -> {
					throw new IllegalStateException(
						"Cannot clear and set the following environment variable at the same time: " + duplicateKeys);
				});
	}

	private void storeOriginalEnvironmentVariables(ExtensionContext context, Collection<String> clearVariables,
			Collection<String> setVariables) {
		context.getStore(NAMESPACE).put(BACKUP, new EnvironmentVariableBackup(clearVariables, setVariables));
	}

	private void reportWarning(ExtensionContext context) {
		boolean wasReported = REPORTED_WARNING.getAndSet(true);
		if (!wasReported)
			context.publishReportEntry(WARNING_KEY, WARNING_VALUE);
	}

	@Override
	public void afterEach(ExtensionContext context) {
		boolean present = Utils
				.annotationPresentOnTestMethod(context, ClearEnvironmentVariable.class, ClearEnvironmentVariables.class,
					SetEnvironmentVariable.class, SetEnvironmentVariables.class);
		if (present) {
			restoreOriginalEnvironmentVariables(context);
		}
	}

	@Override
	public void afterAll(ExtensionContext context) {
		restoreOriginalEnvironmentVariables(context);
	}

	private void restoreOriginalEnvironmentVariables(ExtensionContext context) {
		context.getStore(NAMESPACE).get(BACKUP, EnvironmentVariableBackup.class).restoreVariables();
	}

	// this method is needed by the tests that verify whether the warning is correctly reported
	static void resetWarning() {
		REPORTED_WARNING.set(false);
	}

	/**
	 * Stores which environment variables need to be cleared or set to their old values after the test.
	 */
	private static class EnvironmentVariableBackup {

		private final Map<String, String> variablesToSet;
		private final Set<String> variablesToUnset;

		public EnvironmentVariableBackup(Collection<String> clearVariables, Collection<String> setVariables) {
			variablesToSet = new HashMap<>();
			variablesToUnset = new HashSet<>();
			Stream.concat(clearVariables.stream(), setVariables.stream()).forEach(variable -> {
				String backup = System.getenv(variable);
				if (backup == null)
					variablesToUnset.add(variable);
				else
					variablesToSet.put(variable, backup);
			});
		}

		public void restoreVariables() {
			EnvironmentVariableUtils.set(variablesToSet);
			EnvironmentVariableUtils.clear(variablesToUnset);
		}

	}

}
