/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.platform.commons.support.AnnotationSupport;

class DefaultLocaleExtension implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback, AfterEachCallback {

	private static final Namespace NAMESPACE = Namespace.create(DefaultLocaleExtension.class);

	private static final String KEY = "DefaultLocale";

	@Override
	public void beforeAll(ExtensionContext context) {
		setDefaultLocale(context);
	}

	@Override
	public void beforeEach(ExtensionContext context) {
		setDefaultLocale(context);
	}

	private void setDefaultLocale(ExtensionContext context) {
		storeDefaultLocale(context);
		Locale configuredLocale = readLocaleFromAnnotation(context);
		Locale.setDefault(configuredLocale);
	}

	private void storeDefaultLocale(ExtensionContext context) {
		context.getStore(NAMESPACE).put(KEY, Locale.getDefault());
	}

	private Locale readLocaleFromAnnotation(ExtensionContext context) {
		Optional<DefaultLocale> annotation = AnnotationSupport.findAnnotation(context.getElement(),
			DefaultLocale.class);
		return annotation.map(DefaultLocaleExtension::createLocale).orElse(Locale.getDefault());
	}

	private static Locale createLocale(DefaultLocale annotation) {
		if (!annotation.country().isEmpty() && !annotation.variant().isEmpty()) {
			return new Locale(annotation.language(), annotation.country(), annotation.variant());
		}
		else if (!annotation.country().isEmpty()) {
			return new Locale(annotation.language(), annotation.country());
		}
		else if (!annotation.variant().isEmpty()) {
			throw new ExtensionConfigurationException(
				"@DefaultLocale.country must not be empty when @DefaultLocale.variant is set!");
		}
		else {
			return new Locale(annotation.language());
		}
	}

	@Override
	public void afterEach(ExtensionContext context) {
		resetDefaultLocale(context);
	}

	@Override
	public void afterAll(ExtensionContext context) {
		resetDefaultLocale(context);
	}

	private void resetDefaultLocale(ExtensionContext context) {
		Locale.setDefault((Locale) context.getStore(NAMESPACE).get(KEY));
	}
}
