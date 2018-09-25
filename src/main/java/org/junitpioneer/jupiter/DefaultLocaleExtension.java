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
import org.junit.platform.commons.support.AnnotationSupport;

class DefaultLocaleExtension implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback, AfterEachCallback {

	private Locale defaultLocale;

	@Override
	public void beforeAll(ExtensionContext context) {
		setDefaultLocale(context);
	}

	@Override
	public void beforeEach(ExtensionContext context) {
		setDefaultLocale(context);
	}

	private void setDefaultLocale(ExtensionContext context) {
		saveDefaultLocale();
		Locale configuredLocale = readLocaleFromAnnotation(context);
		Locale.setDefault(configuredLocale);
	}

	private void saveDefaultLocale() {
		defaultLocale = Locale.getDefault();
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
		resetDefaultLocale();
	}

	@Override
	public void afterAll(ExtensionContext context) {
		resetDefaultLocale();
	}

	private void resetDefaultLocale() {
		Locale.setDefault(defaultLocale);
	}
}
