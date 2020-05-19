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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

class DisabledIfNameExtensionTests {

	@Nested
	class SubstringTests {

		@Test
		void single_correctTestsSkipped() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(SubstringTestCases.class, "single", "java.lang.String");

			assertThat(results.numberOfFailedTests()).isEqualTo(0);
			assertThat(results.numberOfSucceededTests()).isEqualTo(1);
			assertThat(results.numberOfSkippedTests()).isEqualTo(5);
		}

		@Test
		void multiple_correctTestsSkipped() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(SubstringTestCases.class, "multiple", "int");

			assertThat(results.numberOfFailedTests()).isEqualTo(0);
			assertThat(results.numberOfSucceededTests()).isEqualTo(3);
			assertThat(results.numberOfSkippedTests()).isEqualTo(2);
		}

	}

	@Nested
	class RegExpTests {

		@Test
		void single_correctTestsSkipped() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(RegExpTestCases.class, "single", "java.lang.String");

			assertThat(results.numberOfFailedTests()).isEqualTo(0);
			assertThat(results.numberOfSucceededTests()).isEqualTo(2);
			assertThat(results.numberOfSkippedTests()).isEqualTo(4);
		}

		@Test
		void multiple_correctTestsSkipped() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(RegExpTestCases.class, "multiple", "int");

			assertThat(results.numberOfFailedTests()).isEqualTo(0);
			assertThat(results.numberOfSucceededTests()).isEqualTo(2);
			assertThat(results.numberOfSkippedTests()).isEqualTo(3);
		}

	}

	// TEST CASES -------------------------------------------------------------------

	static class SubstringTestCases {

		//@formatter:off
		@DisableIfDisplayName("disable")
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

		@DisableIfDisplayName({ "1", "2" })
		@ParameterizedTest(name = "See if enabled with {0}")
		@ValueSource(ints = { 1, 2, 3, 4, 5 })
		void multiple(int num) {
			if (num == 1 || num == 2)
				fail("Test should've been disabled for " + num);
		}

	}

	static class RegExpTestCases {

		//@formatter:off
		@DisableIfDisplayName(value = ".*disabled?\\s.*", isRegEx = true)
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

		@DisableIfDisplayName(value = { ".*10[^0]*", ".*10{3,4}[^0]*" }, isRegEx = true)
		@ParameterizedTest(name = "See if enabled with {0}")
		@ValueSource(ints = { 1, 10, 100, 1_000, 10_000 })
		void multiple(int num) {
			if (num == 10 || num == 1_000 || num == 10_000)
				fail("Test should've been disabled for " + num);
		}

	}

}
