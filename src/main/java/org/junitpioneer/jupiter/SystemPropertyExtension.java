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

import java.util.Properties;
import java.util.function.Function;

class SystemPropertyExtension
		extends AbstractEntryBasedExtension<String, String,
				ClearSystemProperty, SetSystemProperty, RestoreSystemProperties> {

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

	/**
	 * This implementation uses the 'Preemptive swap' strategy.
	 * <p>
	 * Since {@link java.util.Properties} allows a wrapped default instance and Object values,
	 * cloning is difficult:
	 * <ul>
	 * <li>It is difficult to tell which values are defaults and which are 'top level',
	 * thus a clone might contain the same effective values, but be flattened without defaults.</li>
	 * <li>Object values in a wrapped default instance cannot be accessed without reflection.</li>
	 * </ul>
	 * The 'Preemptive swap' strategy ensure that the original Properties are restored, however
	 * complex they were.  Any artifacts resulting from a flattened default structure are limited
	 * to the context of the test.
	 * <p>
	 * See {@link AbstractEntryBasedExtension#prepareToEnterRestorableContext} for more details.
	 *
	 * @return The original System.Properties object.
	 */
	@Override
	protected Properties prepareToEnterRestorableContext() {

		System.out.println("prepareToEnterRestorableContext");

		final Properties current = System.getProperties();
		final Properties clone = cloneProperties(current);

		System.setProperties(clone);

		return current;
	}

	/**
	 * Create a complete clone of the passed original Properties.
	 * <p>
	 * The clone should have the same effective values, but may not use the same wrapped default
	 * structure as the original.
	 *
	 * @param original
	 * @return
	 */
	Properties cloneProperties(final Properties original) {
		final Properties clone = new Properties();

		// System.getProperties() returns the actual Properties object, not a copy.
		// Clone doesn't include defaults, but propertyNames() does.
		original.propertyNames().asIterator().forEachRemaining(k -> {
			clone.put(k, original.getProperty(k.toString()));
		});

		return clone;
	}

	@Override
	protected void prepareToExitRestorableContext(final Properties properties) {
		System.out.println("prepareToExitRestorableContext");
		System.setProperties(properties);
	}

}
