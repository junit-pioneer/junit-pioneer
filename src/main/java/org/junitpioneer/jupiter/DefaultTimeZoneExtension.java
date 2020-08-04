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

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

class DefaultTimeZoneExtension implements BeforeEachCallback, AfterEachCallback {

	private static final Namespace NAMESPACE = Namespace.create(DefaultTimeZoneExtension.class);

	private static final String KEY = "DefaultTimeZone";

	@Override
	public void beforeEach(ExtensionContext context) {
		PioneerAnnotationUtils
				.findClosestEnclosingAnnotation(context, DefaultTimeZone.class)
				.ifPresent(annotation -> setDefaultTimeZone(context.getStore(NAMESPACE), annotation));
	}

	private void setDefaultTimeZone(Store store, DefaultTimeZone annotation) {
		storeDefaultTimeZone(store);
		String timeZoneId = annotation.value();
		TimeZone.setDefault(getTimeZone(timeZoneId));
	}

	private TimeZone getTimeZone(String timeZoneId) {
		TimeZone configuredTimeZone = TimeZone.getTimeZone(timeZoneId);
		// TimeZone::getTimeZone returns with GMT as fallback if the given ID cannot be understood
		if (configuredTimeZone.equals(TimeZone.getTimeZone("GMT")) && !timeZoneId.equals("GMT")) {
			throw new ExtensionConfigurationException(String
					.format("@DefaultTimeZone not configured correctly. "
							+ "Could not find the specified time zone + '%s'. "
							+ "Please use correct identifiers, e.g. \"GMT\" for Greenwich Mean Time.",
						timeZoneId));
		}
		return configuredTimeZone;
	}

	private void storeDefaultTimeZone(Store store) {
		store.put(KEY, TimeZone.getDefault());
	}

	@Override
	public void afterEach(ExtensionContext context) {
		PioneerAnnotationUtils
				.findClosestEnclosingAnnotation(context, DefaultTimeZone.class)
				.ifPresent(__ -> resetDefaultTimeZone(context.getStore(NAMESPACE)));
	}

	private void resetDefaultTimeZone(Store store) {
		TimeZone.setDefault(store.get(KEY, TimeZone.class));
	}

}
