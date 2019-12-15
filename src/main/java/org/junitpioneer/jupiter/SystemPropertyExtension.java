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

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.platform.commons.support.AnnotationSupport;

class SystemPropertyExtension implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback, AfterEachCallback {

	private static final Namespace NAMESPACE = Namespace.create(SystemPropertyExtension.class);

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		handleSystemProperties(context);
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		if (annotationsPresentOnTestMethod(context)) {
			handleSystemProperties(context);
		}
	}

	private boolean annotationsPresentOnTestMethod(ExtensionContext context) {
		//@formatter:off
		return context.getTestMethod()
				.map(testMethod -> AnnotationSupport.isAnnotated(testMethod, ClearSystemProperty.class)
						|| AnnotationSupport.isAnnotated(testMethod, ClearSystemProperties.class)
						|| AnnotationSupport.isAnnotated(testMethod, SetSystemProperty.class)
						|| AnnotationSupport.isAnnotated(testMethod, SetSystemProperties.class))
				.orElse(false);
		//@formatter:on
	}

	private void handleSystemProperties(ExtensionContext context) {
		List<ClearSystemProperty> clearAnnotations = findRepeatableAnnotations(context, ClearSystemProperty.class);
		List<SetSystemProperty> setAnnotations = findRepeatableAnnotations(context, SetSystemProperty.class);

		storeOriginalSystemProperties(context, clearAnnotations, setAnnotations);
		clearAnnotatedSystemProperties(clearAnnotations);
		setAnnotatedSystemProperties(setAnnotations);
	}

	private <A extends Annotation> List<A> findRepeatableAnnotations(ExtensionContext context,
			Class<A> annotationType) {
		// @formatter:off
		return context.getElement()
				.map(element -> AnnotationSupport.findRepeatableAnnotations(element, annotationType))
				.orElseGet(Collections::emptyList);
		// @formatter:on
	}

	private void storeOriginalSystemProperties(ExtensionContext context, List<ClearSystemProperty> clearAnnotations,
			List<SetSystemProperty> setAnnotations) {
		Set<String> clearKeys = clearAnnotations.stream().map(ClearSystemProperty::key) //
				.collect(Collectors.toCollection(HashSet::new));
		Set<String> setKeys = setAnnotations.stream().map(SetSystemProperty::key) //
				.collect(Collectors.toCollection(HashSet::new));
		Store store = context.getStore(NAMESPACE);

		Stream.concat(clearKeys.stream(), setKeys.stream()).forEach(key -> {
			String backup = System.getProperty(key);
			store.put(key, backup);
		});

		clearKeys.stream() //
				.filter(setKeys::contains) //
				.reduce((k0, k1) -> k0 + ", " + k1) //
				.ifPresent(duplicateKeys -> {
					throw new ExtensionConfigurationException(
						"Cannot clear and set the following system properties at the same time: " + duplicateKeys);
				});
	}

	private void clearAnnotatedSystemProperties(List<ClearSystemProperty> clearAnnotations) {
		clearAnnotations.forEach(prop -> System.clearProperty(prop.key()));
	}

	private void setAnnotatedSystemProperties(List<SetSystemProperty> setAnnotations) {
		setAnnotations.forEach(prop -> System.setProperty(prop.key(), prop.value()));
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		if (annotationsPresentOnTestMethod(context)) {
			resetOriginalSystemProperties(context);
		}
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		resetOriginalSystemProperties(context);
	}

	private void resetOriginalSystemProperties(ExtensionContext context) {
		// @formatter:off
		Stream<String> clearKeys = findRepeatableAnnotations(context, ClearSystemProperty.class).stream()
				.map(ClearSystemProperty::key);
		Stream<String> setKeys = findRepeatableAnnotations(context, SetSystemProperty.class).stream()
				.map(SetSystemProperty::key);
		// @formatter:on
		Store store = context.getStore(NAMESPACE);

		Stream.concat(clearKeys, setKeys).forEach(key -> {
			String backup = store.get(key, String.class);
			if (backup == null) {
				System.clearProperty(key);
			}
			else {
				System.setProperty(key, backup);
			}
		});
	}

}
