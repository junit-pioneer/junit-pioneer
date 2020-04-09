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
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

class DefaultTimeZoneExtension implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback, AfterEachCallback {

	private static final Namespace NAMESPACE = Namespace.create(DefaultTimeZoneExtension.class);

	private static final String KEY = "DefaultTimeZone";

	@Override
	public void beforeAll(ExtensionContext context) {
		PioneerAnnotationUtils
				.findClosestEnclosingAnnotation(context, DefaultTimeZone.class)
				.ifPresent(annotation -> setDefaultTimeZone(context.getStore(NAMESPACE), annotation));
	}

	@Override
	public void beforeEach(ExtensionContext context) {
		PioneerAnnotationUtils
				.findClosestEnclosingAnnotation(context, DefaultTimeZone.class)
				.ifPresent(annotation -> setDefaultTimeZone(context.getStore(NAMESPACE), annotation));
	}

	private void setDefaultTimeZone(Store store, DefaultTimeZone annotation) {
		storeDefaultTimeZone(store);
		TimeZone configuredTimeZone = TimeZone.getTimeZone(annotation.value());
		TimeZone.setDefault(configuredTimeZone);
	}

	private void storeDefaultTimeZone(Store store) {
		store.put(KEY, TimeZone.getDefault());
	}

	@Override
	public void afterEach(ExtensionContext context) {
		if (PioneerAnnotationUtils.isAnyAnnotationPresent(context, DefaultTimeZone.class)) {
			resetDefaultTimeZone(context.getStore(NAMESPACE));
		}
	}

	@Override
	public void afterAll(ExtensionContext context) {
		resetDefaultTimeZone(context.getStore(NAMESPACE));
	}

	private void resetDefaultTimeZone(Store store) {
		TimeZone.setDefault(store.get(KEY, TimeZone.class));
	}

}
