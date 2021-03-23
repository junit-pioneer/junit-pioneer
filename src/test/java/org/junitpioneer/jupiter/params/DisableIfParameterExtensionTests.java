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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
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
		@DisplayName("does not intercept non-parameterized tests")
		void simpleTest() {
			ExecutionResults results = PioneerTestKit.executeTestMethod(BadConfigTestCases.class, "simpleTest");

			assertThat(results).hasSingleSucceededTest();
		}

		@Test
		@DisplayName("throws an exception if method has no parameters")
		void noParameters() {
			ExecutionResults results = PioneerTestKit.executeTestMethod(BadConfigTestCases.class, "noParameters");

			assertThat(results)
					.hasSingleFailedTest()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessageContainingAll("Can't disable based on arguments", "had no parameters");
		}

		@Test
		@DisplayName("throws an exception if both 'matches' and 'contains' is missing for DisableIfParameter")
		void missingValues() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigTestCases.class, "missingInputs", String.class);

			assertThat(results)
					.hasNumberOfFailedTests(3)
					.withExceptionInstancesOf(ExtensionConfigurationException.class)
					.allMatch(("DisableIfParameter requires that either `contains` or `matches` is set.")::equals);
		}

		@Test
		@DisplayName("throws an exception if both 'matches' and 'contains' is set for DisableIfParameter")
		void bothValuesSet() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigTestCases.class, "setBothParams", String.class);

			assertThat(results)
					.hasNumberOfFailedTests(3)
					.withExceptionInstancesOf(ExtensionConfigurationException.class)
					.allMatch(("DisableIfParameter requires that either `contains` or `matches` is set.")::equals);
		}

		@Test
		@DisplayName("throws an exception if it can not find the parameter based on the given name for DisableIfParameter")
		void missingParameter() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigTestCases.class, "badlyNamedParam", String.class);

			assertThat(results)
					.hasNumberOfFailedTests(3)
					.withExceptions()
					.allMatch(s -> s.contains("Could not resolve parameter"));
		}

	}

	static class CorrectConfigTestCases {

		@ParameterizedTest
		@DisableIfParameter(contains = "She")
		@MethodSource("requiescat")
		void interceptContainsImplicitIndex(String line, String line2, TestReporter reporter) {
			// implicit index - disable if first parameter contains "She"
			assertThat(line2).doesNotContain("She");
		}

		@ParameterizedTest
		@DisableIfParameter(index = 1, contains = "her")
		@MethodSource("requiescat")
		void interceptContainsExplicitIndex(String line, String line2) {
			// explicit index - disable if second parameter contains "her"
			assertThat(line).doesNotContain("her");
		}

		@ParameterizedTest
		@DisableIfAnyParameter(contains = { "She" })
		@MethodSource("requiescat")
		void interceptContainsAny(String line, String line2) {
			assertThat(line).doesNotContain("She");
			assertThat(line2).doesNotContain("She");
		}

		@ParameterizedTest
		@DisableIfAllParameters(contains = { "She", "she" })
		@MethodSource("requiescat")
		void interceptContainsAll(String line, String line2) {
		}

		@ParameterizedTest
		@DisableIfParameter(matches = { ".*[tr].?" })
		@MethodSource("requiescat")
		void interceptMatches(String line, String line2) {
			assertThat(line).doesNotMatch(".*[tr].?");
			assertThat(line2).doesNotMatch(".*[tr].?");
		}

		@ParameterizedTest
		@DisableIfAnyParameter(matches = ".*here?.*")
		@MethodSource("requiescat")
		void interceptMatchesAny(String line, String line2) {
			assertThat(line).doesNotContainPattern("here?");
			assertThat(line2).doesNotContainPattern("here?");
		}

		@ParameterizedTest
		@DisableIfAllParameters(matches = ".*[Ss]he.*")
		@MethodSource("requiescat")
		void interceptMatchesAll(String line, String line2) {
			assertThat(line).doesNotContainPattern(".*[Ss]he.*");
			assertThat(line2).doesNotContainPattern(".*[Ss]he.*");
		}

		@ParameterizedTest
		@DisableIfParameter(index = 1, contains = "she")
		@DisableIfParameter(index = 0, contains = "her")
		@MethodSource("requiescat")
		void indexedIntercept(String line, String line2) {
			assertThat(line).doesNotContain("her");
			assertThat(line2).doesNotContain("she");
		}

		static Stream<Arguments> requiescat() {
			return Stream
					.of(Arguments.of("Tread lightly, she is near", "Under the snow,"),
						Arguments.of("Speak gently, she can hear", "The daisies grow."),
						Arguments.of("All her bright golden hair", "Tarnished with rust,"),
						Arguments.of("She that was young and fair", "Fallen to dust."),
						Arguments.of("Lily-like, white as snow,", "She hardly knew"),
						Arguments.of("She was a woman, so", "Sweetly she grew."),
						Arguments.of("Coffin-board, heavy stone,", "Lie on her breast,"),
						Arguments.of("I vex my heart alone,", "She is at rest."),
						Arguments.of("Peace, peace, she cannot hear", "Lyre or sonnet,"),
						Arguments.of("All my life's buried here,", "Heap earth upon it."));
		}

	}

	static class BadConfigTestCases {

		@Test
		@DisableIfAnyParameter(contains = "A")
		void simpleTest() {
		}

		@ParameterizedTest
		@DisableIfParameter(index = 0, contains = "aaa")
		@ValueSource(strings = { "aaa" })
		void noParameters() {
		}

		@ParameterizedTest
		@DisableIfAnyParameter(contains = "a")
		@ValueSource(strings = { "aaa" })
		void noParametersAny() {
		}

		@ParameterizedTest
		@DisableIfAllParameters(contains = "a")
		@ValueSource(strings = { "aaa" })
		void noParametersAll() {
		}

		@ParameterizedTest
		@DisableIfParameter
		@ValueSource(strings = { "A", "B", "C" })
		void missingInputs(String value) {
		}

		@ParameterizedTest
		@DisableIfAnyParameter
		@ValueSource(strings = { "A", "B", "C" })
		void missingInputsAny(String value) {
		}

		@ParameterizedTest
		@DisableIfAllParameters
		@ValueSource(strings = { "A", "B", "C" })
		void missingInputsAll(String value) {
		}

		@ParameterizedTest
		@DisableIfParameter(name = "param", contains = { "A" })
		@ValueSource(strings = { "A", "B", "C" })
		void badlyNamedParam(String value) {
		}

		@ParameterizedTest
		@DisableIfParameter(contains = { "sonnet", "life" }, matches = "^.*(Peace, )\\1.*$")
		@ValueSource(strings = { "A", "B", "C" })
		void setBothParams(String value) {
		}

	}

}
