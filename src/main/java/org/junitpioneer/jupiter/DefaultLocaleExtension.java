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

import java.util.Locale;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junitpioneer.internal.PioneerAnnotationUtils;

class DefaultLocaleExtension implements BeforeEachCallback, AfterEachCallback {

	private static final Namespace NAMESPACE = Namespace.create(DefaultLocaleExtension.class);

	private static final String KEY = "DefaultLocale";

	@Override
	public void beforeEach(ExtensionContext context) {
		PioneerAnnotationUtils
				.findClosestEnclosingAnnotation(context, DefaultLocale.class)
				.ifPresent(annotation -> setDefaultLocale(context, annotation));
	}

	private void setDefaultLocale(ExtensionContext context, DefaultLocale annotation) {
		Locale configuredLocale = createLocale(annotation);
		// defer storing the current default locale until the new locale could be created from the configuration
		// (this prevents cases where misconfigured extensions store default locale now and restore it later,
		// which leads to race conditions in our tests)
		storeDefaultLocale(context);
		Locale.setDefault(configuredLocale);
	}

	private void storeDefaultLocale(ExtensionContext context) {
		context.getStore(NAMESPACE).put(KEY, Locale.getDefault());
	}

	private static Locale createLocale(DefaultLocale annotation) {
		if (!annotation.value().isEmpty()) {
			return createFromLanguageTag(annotation);
		} else {
			return createFromParts(annotation);
		}
	}

	private static Locale createFromLanguageTag(DefaultLocale annotation) {
		if (!annotation.language().isEmpty() || !annotation.country().isEmpty() || !annotation.variant().isEmpty()) {
			throw new ExtensionConfigurationException(
				"@DefaultLocale can only be used with language tag if language, country, and variant are not set");
		}
		return Locale.forLanguageTag(annotation.value());
	}

	private static Locale createFromParts(DefaultLocale annotation) {
		String language = annotation.language();
		String country = annotation.country();
		String variant = annotation.variant();
		if (!language.isEmpty() && !country.isEmpty() && !variant.isEmpty()) {
			return new Locale(language, country, variant);
		} else if (!language.isEmpty() && !country.isEmpty()) {
			return new Locale(language, country);
		} else if (!language.isEmpty() && variant.isEmpty()) {
			return new Locale(language);
		} else {
			throw new ExtensionConfigurationException(
				"@DefaultLocale not configured correctly. When not using a language tag, specify either"
						+ "language, or language and country, or language and country and variant.");
		}
	}

	@Override
	public void afterEach(ExtensionContext context) {
		PioneerAnnotationUtils
				.findClosestEnclosingAnnotation(context, DefaultLocale.class)
				.ifPresent(__ -> resetDefaultLocale(context));
	}

	private void resetDefaultLocale(ExtensionContext context) {
		Locale defaultLocale = context.getStore(NAMESPACE).get(KEY, Locale.class);
		// default locale is null if the extension was misconfigured and execution failed in "before"
		if (defaultLocale != null)
			Locale.setDefault(defaultLocale);
	}

}
