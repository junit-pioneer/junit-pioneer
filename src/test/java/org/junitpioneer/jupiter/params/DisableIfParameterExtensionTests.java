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

/**
 * Oscar Wilde: Requiescat is in the public domain
 */
@DisplayName("DisableIfParameterExtension")
class DisableIfParameterExtensionTests {

	@Nested
	@DisplayName("when configured correctly")
	class CorrectConfigurationTests {

		@Test
		@DisplayName("disables tests when parameter contains any value from the 'contains' array")
		void interceptContains() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CorrectConfigTestCases.class, "interceptContains",
						String.class);

			assertThat(results).hasNumberOfSucceededTests(2).hasNumberOfAbortedTests(2);
		}

		@Test
		@DisplayName("disables tests when any parameter contains any value from the 'contains' array")
		void interceptContainsAny() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CorrectConfigTestCases.class, "interceptContainsAny",
						String.class, String.class);

			assertThat(results).hasNumberOfAbortedTests(2);
		}

		@Test
		@DisplayName("disables tests when parameter matches any regex from the 'matches' array")
		void interceptMatches() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CorrectConfigTestCases.class, "interceptMatches",
						String.class);

			assertThat(results).hasNumberOfSucceededTests(2).hasNumberOfAbortedTests(2);
		}

		@Test
		@DisplayName("disables tests when any parameter matches any regex from the 'matches' array")
		void interceptMatchesAny() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CorrectConfigTestCases.class, "interceptMatchesAny",
						String.class, String.class);

			assertThat(results).hasNumberOfAbortedTests(2);
		}

		@Test
		@DisplayName("disables tests if parameter matches regex from 'matches' or contains value from 'contains'")
		void interceptMatchesAndContains() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CorrectConfigTestCases.class, "interceptBoth", String.class);

			assertThat(results).hasNumberOfSucceededTests(1).hasNumberOfAbortedTests(3);
		}

	}

	@Nested
	@DisplayName("when not configured correctly")
	class MisconfigurationTests {

		@Test
		@DisplayName("throws an exception if both 'matches' and 'contains' is missing")
		void missingValues() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigTestCases.class, "missingValues", String.class);

			assertThat(results).hasNumberOfFailedTests(3);
		}

	}

	static class CorrectConfigTestCases {

		@ParameterizedTest
		@DisableIfParameter(contains = "she")
		@ValueSource(strings = { "Tread lightly, she is near", "Under the snow,", "Speak gently, she can hear",
				"The daisies grow." })
		void interceptContains(String line) {
		}

		@ParameterizedTest
		@DisableIfParameter(contains = { "bright", "dust" })
		@CsvSource(delimiter = ';', value = { "All her bright golden hair;Tarnished with rust,",
				"She that was young and fair;Fallen to dust." })
		void interceptContainsAny(String line, String line2) {
		}

		@ParameterizedTest
		@DisableIfParameter(matches = { ".*knew", ".*grew" })
		@ValueSource(strings = { "Lily-like, white as snow,", "She hardly knew", "She was a woman, so",
				"Sweetly she grew" })
		void interceptMatches(String value) {
		}

		@ParameterizedTest
		@DisableIfParameter(matches = ".*hea?rt?.*")
		@CsvSource(delimiter = ';', value = { "Coffin-board, heavy stone,;Lie on her breast,",
				"I vex my heart alone;She is at rest." })
		void interceptMatchesAny(String line, String line2) {
		}

		@ParameterizedTest
		@DisableIfParameter(contains = { "sonnet", "life" }, matches = "^.*(Peace, )\\1.*$")
		@ValueSource(strings = { "Peace, Peace, she cannot hear", "Lyre or sonnet,", "All my life’s buried here,",
				"Heap earth upon it." })
		void interceptBoth(String value) {
		}

	}

	static class BadConfigTestCases {

		@ParameterizedTest
		@DisableIfParameter
		@ValueSource(strings = { "A", "B", "C" })
		void missingValues(String value) {
		}

	}

}
