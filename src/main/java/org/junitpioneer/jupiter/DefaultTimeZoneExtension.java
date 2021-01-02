/*
 * Copyright 2016-2021 the original author or authors.
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
		TimeZone defaultTimeZone = createTimeZone(annotation.value());
		// defer storing the current default time zone until the new time zone could be created from the configuration
		// (this prevents cases where misconfigured extensions store default time zone now and restore it later,
		// which leads to race conditions in our tests)
		storeDefaultTimeZone(store);
		TimeZone.setDefault(defaultTimeZone);
	}

	private static TimeZone createTimeZone(String timeZoneId) {
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
		TimeZone timeZone = store.get(KEY, TimeZone.class);
		// default time zone is null if the extension was misconfigured and execution failed in "before"
		if (timeZone != null)
			TimeZone.setDefault(timeZone);
	}

}
