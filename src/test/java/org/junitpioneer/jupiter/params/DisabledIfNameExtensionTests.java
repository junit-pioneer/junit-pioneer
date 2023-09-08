/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.params;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

@ResourceLock(value = "org.junitpioneer.jupiter.params.PioneerAnnotationUtils")
@DisplayName("DisableIfNameExtension")
class DisabledIfNameExtensionTests {

	@Nested
	@DisplayName("when matching substrings")
	class SubstringTests {

		@Test
		@DisplayName("disables a single test correctly")
		void single_correctTestsSkipped() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(SubstringTestCases.class, "single", String.class);

			assertThat(results).hasNumberOfFailedTests(0).hasNumberOfSucceededTests(1).hasNumberOfSkippedTests(5);
		}

		@Test
		@DisplayName("disables multiple tests correctly")
		void multiple_correctTestsSkipped() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(SubstringTestCases.class, "multiple", int.class);

			assertThat(results).hasNumberOfFailedTests(0).hasNumberOfSucceededTests(3).hasNumberOfSkippedTests(2);
		}

		@Test
		@DisplayName("does not skip the entire container based on test method name, only display name")
		void methodNameContainsSubstring_containerNotSkipped() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(SubstringTestCases.class, "methodNameContains", int.class);

			assertThat(results).hasNumberOfStartedTests(3);
		}

	}

	@Nested
	@DisplayName("when matching regular expressions")
	class RegExpTests {

		@Test
		@DisplayName("disables a single test correctly")
		void single_correctTestsSkipped() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(RegExpTestCases.class, "single", String.class);

			assertThat(results).hasNumberOfFailedTests(0).hasNumberOfSucceededTests(2).hasNumberOfSkippedTests(4);
		}

		@Test
		@DisplayName("disables multiple tests correctly")
		void multiple_correctTestsSkipped() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(RegExpTestCases.class, "multiple", int.class);

			assertThat(results).hasNumberOfFailedTests(0).hasNumberOfSkippedTests(3).hasNumberOfSucceededTests(2);
		}

		@Test
		@DisplayName("does not skip the entire container based on test method name, only display name")
		void methodNameMatchesRegExp_containerNotSkipped() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(RegExpTestCases.class, "methodNameMatches", int.class);

			assertThat(results).hasNumberOfStartedTests(3);
		}

	}

	@Nested
	@DisplayName("when configured incorrectly")
	class MisconfigurationTests {

		@Test
		@DisplayName("throws an exception if no matches/contains is specified")
		void noContainsNoMatches_configurationException() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(ConfigurationTestCases.class, "noContainsNoMatches",
						String.class);

			assertThat(results).hasSingleFailedTest();
		}

		@Test
		@DisplayName("throws an exception if both matches and contains are specified")
		void containsAndMatches_contains_correctTestsSkipped() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(ConfigurationTestCases.class, "containsAndMatches", int.class);

			assertThat(results)
					.hasNumberOfFailedTests(5)
					.withExceptions()
					.allMatch(s -> s.contains("but both are present."));
		}

	}

	// TEST CASES -------------------------------------------------------------------

	static class SubstringTestCases {

		//@formatter:off
		@DisableIfDisplayName(contains = "disable")
		@ParameterizedTest(name = "See if enabled with {0}")
		@ValueSource(
				strings = {
						"disable who",
						"you, disable you",
						"why am I disabled",
						"what has been disabled must stay disabled",
						"fine disable me all you want",
						"not those one, though!"
				}
		)
		//@formatter:on
		void single(String reason) {
			if (reason.contains("disable"))
				fail("Test should've been disabled " + reason);
		}

		@DisableIfDisplayName(contains = { "1", "2" })
		@ParameterizedTest(name = "See if enabled with {0}")
		@ValueSource(ints = { 1, 2, 3, 4, 5 })
		void multiple(int number) {
			if (number == 1 || number == 2)
				fail("Test should've been disabled for " + number);
		}

		@DisableIfDisplayName(contains = "Contains")
		@ParameterizedTest(name = "See if enabled with {0}")
		@ValueSource(ints = { 1, 2, 3 })
		void methodNameContains(int unusedParameter) {
		}

	}

	static class RegExpTestCases {

		//@formatter:off
		@DisableIfDisplayName(matches = ".*disabled?\\s.*")
		@ParameterizedTest(name = "See if enabled with {0}")
		@ValueSource(
				strings = {
						"disable who",
						"you, disable you",
						"why am I disabled",
						"what has been disabled must stay disabled",
						"fine disable me all you want",
						"not those one, though!"
				}
		)
		void single(String reason) {
			boolean shouldBeDisabled = Arrays
					.asList(
							"you, disable you",
							"what has been disabled must stay disabled",
							"fine disable me all you want")
					.contains(reason);
			if (shouldBeDisabled)
				fail("Test should've been disabled " + reason);
		}
		//@formatter:on

		@DisableIfDisplayName(matches = { ".*10[^0]*", ".*10{3,4}[^0]*" })
		@ParameterizedTest(name = "See if enabled with {0}")
		@ValueSource(ints = { 1, 10, 100, 1_000, 10_000 })
		void multiple(int number) {
			if (number == 10 || number == 1_000 || number == 10_000)
				fail("Test should've been disabled for " + number);
		}

		@DisableIfDisplayName(matches = ".*Matches.*")
		@ParameterizedTest(name = "See if enabled with {0}")
		@ValueSource(ints = { 1, 2, 3 })
		void methodNameMatches(int unusedParameter) {
		}

	}

	static class ConfigurationTestCases {

		@DisableIfDisplayName
		@ParameterizedTest
		@ValueSource(strings = "a string")
		void noContainsNoMatches(String reason) {
			fail("Test should never have been executed because of misconfiguration.");
		}

		@DisableIfDisplayName(contains = { "1", "2" }, matches = "\\w*")
		@ParameterizedTest(name = "See if enabled with {0}")
		@ValueSource(ints = { 1, 2, 3, 4, 5 })
		void containsAndMatches(int number) {
			if (number == 1 || number == 2)
				fail("Test should've been disabled for " + number);
		}

	}

}
