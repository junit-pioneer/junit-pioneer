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

import java.util.TimeZone;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

class DefaultTimeZoneExtension implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback, AfterEachCallback {

	private static final Namespace NAMESPACE = Namespace.create(DefaultTimeZoneExtension.class);

	private static final String KEY = "DefaultTimeZone";

	@Override
	public void beforeAll(ExtensionContext context) {
		setDefaultTimeZone(context);
	}

	@Override
	public void beforeEach(ExtensionContext context) {
		if (Utils.annotationPresentOnTestMethod(context, DefaultTimeZone.class)) {
			setDefaultTimeZone(context);
		}
	}

	private void setDefaultTimeZone(ExtensionContext context) {
		storeDefaultTimeZone(context);
		TimeZone configuredTimeZone = readTimeZoneFromAnnotation(context);
		TimeZone.setDefault(configuredTimeZone);
	}

	private void storeDefaultTimeZone(ExtensionContext context) {
		context.getStore(NAMESPACE).put(KEY, TimeZone.getDefault());
	}

	private TimeZone readTimeZoneFromAnnotation(ExtensionContext context) {
		return Utils
				.findAnnotation(context, DefaultTimeZone.class)
				.map(DefaultTimeZone::value)
				.map(TimeZone::getTimeZone)
				.orElseThrow(() -> new ExtensionConfigurationException(
					"The extension is active, but the corresponding annotation could not be found. (This may be a bug.)"));
	}

	@Override
	public void afterEach(ExtensionContext context) {
		if (Utils.annotationPresentOnTestMethod(context, DefaultTimeZone.class)) {
			resetDefaultTimeZone(context);
		}
	}

	@Override
	public void afterAll(ExtensionContext context) {
		resetDefaultTimeZone(context);
	}

	private void resetDefaultTimeZone(ExtensionContext context) {
		TimeZone.setDefault(context.getStore(NAMESPACE).get(KEY, TimeZone.class));
	}

}
