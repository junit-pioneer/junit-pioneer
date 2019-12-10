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

import java.lang.reflect.Method;
import java.util.Properties;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.platform.commons.support.AnnotationSupport;

class SystemPropertyExtension implements BeforeEachCallback, AfterEachCallback {

	private static final Namespace NAMESPACE = Namespace.create(SystemPropertyExtension.class);

	private static final String KEY = "SystemProperty";

	@Override
	public void beforeEach(final ExtensionContext context) throws Exception {
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

	private void handleSystemProperties(final ExtensionContext context) {
		storeOriginalSystemProperties(context);
		context.getTestMethod().ifPresent(testMethod -> {
			clearAnnotatedSystemProperties(testMethod);
			setAnnotatedSystemProperties(testMethod);
		});
	}

	private void storeOriginalSystemProperties(final ExtensionContext context) {
		final Properties backup = new Properties();
		backup.putAll(System.getProperties());
		context.getStore(NAMESPACE).put(KEY, backup);
	}

	private void clearAnnotatedSystemProperties(final Method testMethod) {
		//@formatter:off
		AnnotationSupport.findRepeatableAnnotations(testMethod, ClearSystemProperty.class).stream()
				.forEach(prop -> System.clearProperty(prop.key()));
		//@formatter:on
	}

	private void setAnnotatedSystemProperties(final Method testMethod) {
		//@formatter:off
		AnnotationSupport.findRepeatableAnnotations(testMethod, SetSystemProperty.class).stream()
				.forEach(prop -> System.setProperty(prop.key(), prop.value()));
		//@formatter:on
	}

	@Override
	public void afterEach(final ExtensionContext context) throws Exception {
		if (annotationsPresentOnTestMethod(context)) {
			resetOriginalSystemProperties(context);
		}
	}

	private void resetOriginalSystemProperties(final ExtensionContext context) {
		final Properties backup = context.getStore(NAMESPACE).get(KEY, Properties.class);
		System.setProperties(backup);
	}

}
