/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.testkit.assertion.reportentry;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ObjectAssert;

/**
 * Assertions for asserting the content of the published report entries.
 */
public interface ReportEntryContentAssert {

	/**
	 * Asserts that the report entry has a specified key and value.
	 * Fails if there are multiple report entries.
	 *
	 * @param key   the key of the expected report entry
	 * @param value the value of the expected report entry
	 */
	void withKeyAndValue(String key, String value);

	/**
	 * Asserts that the report entries contain exactly the specified values (in any order).
	 *
	 * @param expected the expected values of the report entries
	 */
	void withValues(String... expected);

	/**
	 * Asserts that the report entries contain the specified key-value pairs (in any order).
	 * Fails if there are odd number of supplied strings.
	 *
	 * @param keyAndValuePairs the expected key-value pairs of the report entries
	 */
	void withKeyValuePairs(String... keyAndValuePairs);

	/**
	 * Asserts that the supplied predicate returns true for the report entries.
	 *
	 * @param predicate the condition we want to fulfill
	 */
	void asserting(Predicate<Map.Entry<String, String>> predicate);

	/**
	 * Applies the supplied consumer to the report entries reported by the tests/containers.
	 *
	 * @param testFunction a consumer, for writing more flexible tests
	 */
	void andThen(BiConsumer<String, String> testFunction);

	/**
	 * Returns a {@link ListAssert} to assert all report entry values.
	 */
	ListAssert<String> values();

	/**
	 * Returns an {@link ObjectAssert} to assert the first value from all report entries.
	 */
	AbstractStringAssert<?> firstValue();

	/**
	 * Returns an {@link ObjectAssert} to assert a random value from all report entries.
	 */
	AbstractStringAssert<?> anyValue();

	/**
	 * Returns an {@link ObjectAssert} to assert the value of the chosen report entry.
	 */
	AbstractStringAssert<?> value(int index);

	/**
	 * Returns a {@link ListAssert} to assert all report entry keys.
	 */
	ListAssert<String> keys();

	/**
	 * Returns an {@link ObjectAssert} to assert the first key from all report entries.
	 */
	AbstractStringAssert<?> firstKey();

	/**
	 * Returns an {@link ObjectAssert} to assert a random key from all report entries.
	 */
	AbstractStringAssert<?> anyKey();

	/**
	 * Returns an {@link ObjectAssert} to assert the key of the chosen report entry.
	 */
	AbstractStringAssert<?> key(int index);

}
