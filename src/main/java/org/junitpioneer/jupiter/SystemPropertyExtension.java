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

import java.util.function.Function;

class SystemPropertyExtension
		extends AbstractEntryBasedExtension<String, String, ClearSystemProperty, SetSystemProperty> {

	@Override
	protected Function<ClearSystemProperty, String> clearKeyMapper() {
		return ClearSystemProperty::key;
	}

	@Override
	protected Function<SetSystemProperty, String> setKeyMapper() {
		return SetSystemProperty::key;
	}

	@Override
	protected Function<SetSystemProperty, String> setValueMapper() {
		return SetSystemProperty::value;
	}

	@Override
	protected void clearEntry(String key) {
		System.clearProperty(key);
	}

	@Override
	protected String getEntry(String key) {
		return System.getProperty(key);
	}

	@Override
	protected void setEntry(String key, String value) {
		System.setProperty(key, value);
	}

}
