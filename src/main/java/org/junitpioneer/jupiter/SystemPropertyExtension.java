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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Properties;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

class SystemPropertyExtension implements BeforeEachCallback, AfterEachCallback {

	private static final Namespace NAMESPACE = Namespace.create(SystemPropertyExtension.class);

	private static final String KEY = "SystemProperty";

	@Override
	public void beforeEach(final ExtensionContext context) throws Exception {
		storeSystemProperties(context);

		final Method testMethod = context.getTestMethod().get();
		if (testMethod.isAnnotationPresent(ClearSystemProperty.class)) {
			final ClearSystemProperty prop = testMethod.getAnnotation(ClearSystemProperty.class);
			System.clearProperty(prop.key());
		}
		else if (testMethod.isAnnotationPresent(ClearSystemProperties.class)) {
			final ClearSystemProperties props = testMethod.getAnnotation(ClearSystemProperties.class);
			Arrays.stream(props.value()).forEach(prop -> System.clearProperty(prop.key()));
		}
		if (testMethod.isAnnotationPresent(SetSystemProperty.class)) {
			final SetSystemProperty prop = testMethod.getAnnotation(SetSystemProperty.class);
			System.setProperty(prop.key(), prop.value());
		}
		else if (testMethod.isAnnotationPresent(SetSystemProperties.class)) {
			final SetSystemProperties props = testMethod.getAnnotation(SetSystemProperties.class);
			Arrays.stream(props.value()).forEach(prop -> System.setProperty(prop.key(), prop.value()));
		}
	}

	private void storeSystemProperties(final ExtensionContext context) {
		final Properties backup = new Properties();
		backup.putAll(System.getProperties());
		context.getStore(NAMESPACE).put(KEY, backup);
	}

	@Override
	public void afterEach(final ExtensionContext context) throws Exception {
		resetSystemProperties(context);
	}

	private void resetSystemProperties(final ExtensionContext context) {
		final Properties backup = context.getStore(NAMESPACE).get(KEY, Properties.class);
		System.setProperties(backup);
	}

}
