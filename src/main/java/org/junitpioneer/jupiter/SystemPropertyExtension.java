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
import java.util.Properties;
import java.util.function.Consumer;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.platform.commons.support.AnnotationSupport;

class SystemPropertyExtension implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback, AfterEachCallback {

	private static final Namespace NAMESPACE = Namespace.create(SystemPropertyExtension.class);

	private static final String KEY = "SystemProperty";

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
		storeOriginalSystemProperties(context);
		clearAnnotatedSystemProperties(context);
		setAnnotatedSystemProperties(context);
	}

	private void storeOriginalSystemProperties(ExtensionContext context) {
		Properties backup = new Properties();
		backup.putAll(System.getProperties());
		context.getStore(NAMESPACE).put(KEY, backup);
	}

	private void clearAnnotatedSystemProperties(ExtensionContext context) {
		forEachAnnotation(context, ClearSystemProperty.class, prop -> System.clearProperty(prop.key()));
	}

	private void setAnnotatedSystemProperties(ExtensionContext context) {
		forEachAnnotation(context, SetSystemProperty.class, prop -> System.setProperty(prop.key(), prop.value()));
	}

	private <A extends Annotation> void forEachAnnotation(ExtensionContext context, Class<A> annotationType,
			Consumer<A> action) {
		context.getElement().ifPresent(
			element -> AnnotationSupport.findRepeatableAnnotations(element, annotationType).stream().forEach(action));
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
		Properties backup = context.getStore(NAMESPACE).get(KEY, Properties.class);
		System.setProperties(backup);
	}

}
