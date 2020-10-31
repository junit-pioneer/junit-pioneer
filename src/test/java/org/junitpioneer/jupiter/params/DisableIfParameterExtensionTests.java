/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.params;

import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

@DisplayName("DisableIfParameterExtension")
class DisableIfParameterExtensionTests {

	@Nested
	@DisplayName("when configured correctly")
	class CorrectConfigurationTests {

		@Test
		@DisplayName("disables tests when parameter contains any value from the 'contains' array")
		void interceptContains() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CorrectConfigTestCases.class, "interceptContains", int.class);

			assertThat(results).hasNumberOfSucceededTests(1).hasNumberOfAbortedTests(2);
		}

		@Test
		@DisplayName("disables tests when parameter matches any regex from the 'matches' array")
		void interceptMatches() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CorrectConfigTestCases.class, "interceptMatches",
						String.class);

			assertThat(results).hasNumberOfSucceededTests(1).hasNumberOfAbortedTests(2);
		}

		@Test
		@DisplayName("disables tests if parameter matches regex from 'matches' or contains value from 'contains'")
		void interceptMatchesAndContains() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CorrectConfigTestCases.class, "interceptBoth", String.class,
						String.class);

			assertThat(results).hasNumberOfSucceededTests(1).hasNumberOfAbortedTests(3);
		}

	}

	@Nested
	@DisplayName("when not configured correctly")
	class MisconfigurationTests {

		@Test
		@DisplayName("throws an exception if both 'matches' and 'contains' is missing")
		void missingValues() {
			ExecutionResults results = PioneerTestKit.executeTestMethodWithParameterTypes(BadConfigTestCases.class, "missingValues", String.class);

			assertThat(results).hasNumberOfFailedTests(3);
		}

	}

	static class CorrectConfigTestCases {

		@ParameterizedTest
		@DisableIfParameter(contains = "1")
		@ValueSource(ints = { 11, 21, 4 })
		void interceptContains(int number) {
		}

		@ParameterizedTest
		@DisableIfParameter(matches = { "[0-9].*", ".*[0-9]" })
		@ValueSource(strings = { "Ends with 9", "1 is the starting point", "Middle 5 doesn't count" })
		void interceptMatches(String value) {
		}

		@ParameterizedTest
		@DisableIfParameter(contains = { "hello", "bye" }, matches = ".*[0-9].*")
		@CsvSource(value = { "ohellooo,world", "bl1b,foo", "bar,bar", "bar,byes" })
		void interceptBoth(String value, String value2) {
		}

	}

	static class BadConfigTestCases {

		@ParameterizedTest
		@DisableIfParameter
		@ValueSource(strings = {"A", "B", "C"})
		void missingValues(String value) {
		}

	}
}
