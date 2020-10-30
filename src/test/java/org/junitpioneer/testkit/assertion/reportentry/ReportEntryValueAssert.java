/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.testkit.assertion.reportentry;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Assertions for asserting the content of the published report entries.
 */
public interface ReportEntryValueAssert {

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
	void andThen(Consumer<Map.Entry<String, String>> testFunction);

}
