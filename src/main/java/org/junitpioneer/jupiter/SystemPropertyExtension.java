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
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.platform.commons.support.AnnotationSupport;

class SystemPropertyExtension implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback, AfterEachCallback {

	private static final Namespace NAMESPACE = Namespace.create(SystemPropertyExtension.class);
	private static final String BACKUP = "Backup";

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		handleSystemProperties(context);
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		boolean present = Utils
				.annotationPresentOnTestMethod(context, ClearSystemProperty.class, ClearSystemProperties.class,
					SetSystemProperty.class, SetSystemProperties.class);
		if (present) {
			handleSystemProperties(context);
		}
	}

	private void handleSystemProperties(ExtensionContext context) {
		Set<String> propertiesToClear;
		Map<String, String> propertiesToSet;
		try {
			propertiesToClear = findRepeatableAnnotations(context, ClearSystemProperty.class)
					.stream()
					.map(ClearSystemProperty::key)
					.collect(Utils.distinctToSet());
			propertiesToSet = findRepeatableAnnotations(context, SetSystemProperty.class)
					.stream()
					.collect(toMap(SetSystemProperty::key, SetSystemProperty::value));
			preventClearAndSetSameSystemProperties(propertiesToClear, propertiesToSet.keySet());
		}
		catch (IllegalStateException ex) {
			throw new ExtensionConfigurationException("Don't clear/set the same property more than once.", ex);
		}

		storeOriginalSystemProperties(context, propertiesToClear, propertiesToSet.keySet());
		clearSystemProperties(propertiesToClear);
		setSystemProperties(propertiesToSet);
	}

	private void preventClearAndSetSameSystemProperties(Collection<String> propertiesToClear,
			Collection<String> propertiesToSet) {
		propertiesToClear
				.stream()
				.filter(propertiesToSet::contains)
				.reduce((k0, k1) -> k0 + ", " + k1)
				.ifPresent(duplicateKeys -> {
					throw new IllegalStateException(
						"Cannot clear and set the following system properties at the same time: " + duplicateKeys);
				});
	}

	private <A extends Annotation> List<A> findRepeatableAnnotations(ExtensionContext context,
			Class<A> annotationType) {
		return context
				.getElement()
				.map(element -> AnnotationSupport.findRepeatableAnnotations(element, annotationType))
				.orElseGet(Collections::emptyList);
	}

	private void storeOriginalSystemProperties(ExtensionContext context, Collection<String> clearProperties,
			Collection<String> setProperties) {
		context.getStore(NAMESPACE).put(BACKUP, new SystemPropertyBackup(clearProperties, setProperties));
	}

	private void clearSystemProperties(Collection<String> clearProperties) {
		clearProperties.forEach(System::clearProperty);
	}

	private void setSystemProperties(Map<String, String> setProperties) {
		setProperties
				.entrySet()
				.forEach(
					propertyWithValue -> System.setProperty(propertyWithValue.getKey(), propertyWithValue.getValue()));
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		boolean present = Utils
				.annotationPresentOnTestMethod(context, ClearSystemProperty.class, ClearSystemProperties.class,
					SetSystemProperty.class, SetSystemProperties.class);
		if (present) {
			restoreOriginalSystemProperties(context);
		}
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		restoreOriginalSystemProperties(context);
	}

	private void restoreOriginalSystemProperties(ExtensionContext context) {
		context.getStore(NAMESPACE).get(BACKUP, SystemPropertyBackup.class).restoreProperties();
	}

	/**
	 * Stores which system properties need to be cleared or set to their old values after the test.
	 */
	private static class SystemPropertyBackup {

		private final Map<String, String> propertiesToSet;
		private final Set<String> propertiesToUnset;

		public SystemPropertyBackup(Collection<String> clearProperties, Collection<String> setProperties) {
			propertiesToSet = new HashMap<>();
			propertiesToUnset = new HashSet<>();
			Stream.concat(clearProperties.stream(), setProperties.stream()).forEach(property -> {
				String backup = System.getProperty(property);
				if (backup == null)
					propertiesToUnset.add(property);
				else
					propertiesToSet.put(property, backup);
			});
		}

		public void restoreProperties() {
			propertiesToSet
					.entrySet()
					.forEach(propertyWithValue -> System
							.setProperty(propertyWithValue.getKey(), propertyWithValue.getValue()));
			propertiesToUnset.forEach(System::clearProperty);
		}

	}

}
