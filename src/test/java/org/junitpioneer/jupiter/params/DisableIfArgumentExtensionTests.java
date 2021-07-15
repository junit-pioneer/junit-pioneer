/*
 * Copyright 2016-2021 the original author or authors.
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.TestKitTest;

/**
 * Oscar Wilde: Requiescat is in the public domain
 */
@DisplayName("DisableIfArgumentExtension")
class DisableIfArgumentExtensionTests {

	@Nested
	@DisplayName("when configured correctly")
	class CorrectConfigurationTests {

		@TestKitTest(testClass = CorrectConfigTestCases.class, method = "interceptContainsExplicitIndex", methodParameterTypes = {
				String.class, String.class })
		@DisplayName("disables tests when parameter targeted by explicit index contains any value from the 'contains' array")
		void interceptContainsExplicitIndex(ExecutionResults results) {
			assertThat(results).hasNumberOfAbortedTests(1).hasNumberOfSucceededTests(7).hasNumberOfFailedTests(2);
		}

		@TestKitTest(testClass = CorrectConfigTestCases.class, method = "interceptContainsImplicitIndex", methodParameterTypes = {
				String.class, String.class })
		@DisplayName("disables tests when parameter targeted by implicit index contains any value from the 'contains' array")
		void interceptContainsImplicitIndex(ExecutionResults results) {
			assertThat(results).hasNumberOfAbortedTests(2).hasNumberOfSucceededTests(6).hasNumberOfFailedTests(2);
		}

		@TestKitTest(testClass = CorrectConfigTestCases.class, method = "interceptContainsAny", methodParameterTypes = {
				String.class, String.class })
		@DisplayName("disables tests when any parameter contains any value from the 'contains' array")
		void interceptContainsAny(ExecutionResults results) {
			assertThat(results).hasNumberOfAbortedTests(4).hasNumberOfSucceededTests(6);
		}

		@TestKitTest(testClass = CorrectConfigTestCases.class, method = "interceptContainsAll", methodParameterTypes = {
				String.class, String.class })
		@DisplayName("disables tests when all parameters contains any value from the 'contains' array")
		void interceptContainsAll(ExecutionResults results) {
			assertThat(results).hasNumberOfAbortedTests(1).hasNumberOfSucceededTests(9);
		}

		@TestKitTest(testClass = CorrectConfigTestCases.class, method = "interceptMatches", methodParameterTypes = {
				String.class, String.class })
		@DisplayName("disables tests when parameter matches any regex from the 'matches' array")
		void interceptMatches(ExecutionResults results) {
			assertThat(results).hasNumberOfAbortedTests(5).hasNumberOfSucceededTests(2).hasNumberOfFailedTests(3);
		}

		@TestKitTest(testClass = CorrectConfigTestCases.class, method = "interceptMatchesAny", methodParameterTypes = {
				String.class, String.class })
		@DisplayName("disables tests when any parameter matches any regex from the 'matches' array")
		void interceptMatchesAny(ExecutionResults results) {
			assertThat(results).hasNumberOfAbortedTests(3).hasNumberOfSucceededTests(7);
		}

		@TestKitTest(testClass = CorrectConfigTestCases.class, method = "interceptMatchesAll", methodParameterTypes = {
				String.class, String.class })
		@DisplayName("disables tests when all parameters match any regex from the 'matches' array")
		void interceptMatchesAll(ExecutionResults results) {
			assertThat(results).hasNumberOfAbortedTests(1).hasNumberOfSucceededTests(2).hasNumberOfFailedTests(7);
		}

	}

	@Nested
	@DisplayName("when not configured correctly")
	class MisconfigurationTests {

		@TestKitTest(testClass = BadConfigTestCases.class, method = "simpleTest")
		@DisplayName("does not intercept non-parameterized tests")
		void simpleTest(ExecutionResults results) {
			assertThat(results).hasSingleSucceededTest();
		}

		@TestKitTest(testClass = BadConfigTestCases.class, method = "overindexed", methodParameterTypes = String.class)
		@DisplayName("throws an exception if DisableIfArgument index is too large")
		void overindex(ExecutionResults results) {
			assertThat(results)
					.hasNumberOfFailedTests(2)
					.withExceptionInstancesOf(ExtensionConfigurationException.class)
					.allMatch(s -> s.startsWith("Annotation has invalid index"));
		}

		@TestKitTest(testClass = BadConfigTestCases.class, method = "multipleTargets", methodParameterTypes = String.class)
		@DisplayName("throws an exception if both index and name is set on DisableIfArgument")
		void multipleTargets(ExecutionResults results) {
			assertThat(results)
					.hasSingleFailedTest()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessageContaining(
						"Using both name and index parameter targeting in a single @DisableIfArgument is not permitted.");
		}

		@TestKitTest(testClass = BadConfigTestCases.class, method = "noParameters")
		@DisplayName("throws an exception if method has no parameters")
		void noParameters(ExecutionResults results) {
			assertThat(results)
					.hasSingleFailedTest()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessageContainingAll("Can't disable based on arguments", "had no parameters");
		}

		@TestKitTest(testClass = BadConfigTestCases.class, method = "missingInputs", methodParameterTypes = String.class)
		@DisplayName("throws an exception if both 'matches' and 'contains' is missing for DisableIfArgument")
		void missingValues(ExecutionResults results) {
			assertThat(results)
					.hasNumberOfFailedTests(3)
					.withExceptionInstancesOf(ExtensionConfigurationException.class)
					.allMatch(("DisableIfArgument requires that either `contains` or `matches` is set.")::equals);
		}

		@TestKitTest(testClass = BadConfigTestCases.class, method = "missingInputsAny", methodParameterTypes = String.class)
		@DisplayName("throws an exception if both 'matches' and 'contains' is missing for DisableIfAnyArgument")
		void missingValuesAny(ExecutionResults results) {
			assertThat(results)
					.hasNumberOfFailedTests(3)
					.withExceptionInstancesOf(ExtensionConfigurationException.class)
					.allMatch(("DisableIfAnyArgument requires that either `contains` or `matches` is set.")::equals);
		}

		@TestKitTest(testClass = BadConfigTestCases.class, method = "missingInputsAll", methodParameterTypes = String.class)
		@DisplayName("throws an exception if both 'matches' and 'contains' is missing for DisableIfAllArguments")
		void missingValuesAll(ExecutionResults results) {
			assertThat(results)
					.hasNumberOfFailedTests(3)
					.withExceptionInstancesOf(ExtensionConfigurationException.class)
					.allMatch(("DisableIfAllArguments requires that either `contains` or `matches` is set.")::equals);
		}

		@TestKitTest(testClass = BadConfigTestCases.class, method = "setBothParams", methodParameterTypes = String.class)
		@DisplayName("throws an exception if both 'matches' and 'contains' is set for DisableIfArgument")
		void bothValuesSet(ExecutionResults results) {
			assertThat(results)
					.hasNumberOfFailedTests(3)
					.withExceptionInstancesOf(ExtensionConfigurationException.class)
					.allMatch(("DisableIfArgument requires that either `contains` or `matches` is set.")::equals);
		}

		@TestKitTest(testClass = BadConfigTestCases.class, method = "setBothParamsAny", methodParameterTypes = String.class)
		@DisplayName("throws an exception if both 'matches' and 'contains' is set for DisableIfAnyArgument")
		void bothValuesSetAny(ExecutionResults results) {
			assertThat(results)
					.hasNumberOfFailedTests(3)
					.withExceptionInstancesOf(ExtensionConfigurationException.class)
					.allMatch(("DisableIfAnyArgument requires that either `contains` or `matches` is set.")::equals);
		}

		@TestKitTest(testClass = BadConfigTestCases.class, method = "setBothParamsAll", methodParameterTypes = String.class)
		@DisplayName("throws an exception if both 'matches' and 'contains' is set for DisableIfAllArguments")
		void bothValuesSetAll(ExecutionResults results) {
			assertThat(results)
					.hasNumberOfFailedTests(3)
					.withExceptionInstancesOf(ExtensionConfigurationException.class)
					.allMatch(("DisableIfAllArguments requires that either `contains` or `matches` is set.")::equals);
		}

		@TestKitTest(testClass = BadConfigTestCases.class, method = "badlyNamedParam", methodParameterTypes = String.class)
		@DisplayName("throws an exception if it can not find the parameter based on the given name for DisableIfArgument")
		void missingParameter(ExecutionResults results) {
			assertThat(results)
					.hasNumberOfFailedTests(3)
					.withExceptions()
					.allMatch(s -> s.contains("Could not resolve parameter"));
		}

		@TestKitTest(testClass = BadConfigTestCases.class, method = "forced", methodParameterTypes = String.class)
		@DisplayName("does not work without required annotation(s)")
		void forcedExtension(ExecutionResults results) {
			assertThat(results)
					.hasSingleFailedTest()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessageContaining(
						"Required at least one of the following: @DisableIfArgument, @DisableIfAllArguments, @DisableIfAnyArgument but found none.");
		}

	}

	static class CorrectConfigTestCases {

		@ParameterizedTest
		@DisableIfArgument(contains = "She")
		@MethodSource("requiescat")
		void interceptContainsImplicitIndex(String line, String line2) {
			// implicit index - disable if first parameter contains "She"
			assertThat(line2).doesNotContain("She");
		}

		@ParameterizedTest
		@DisableIfArgument(index = 1, contains = "her")
		@MethodSource("requiescat")
		void interceptContainsExplicitIndex(String line, String line2) {
			// explicit index - disable if second parameter contains "her"
			assertThat(line).doesNotContain("her");
		}

		@ParameterizedTest
		@DisableIfAnyArgument(contains = { "She" })
		@MethodSource("requiescat")
		void interceptContainsAny(String line, String line2) {
			assertThat(line).doesNotContain("She");
			assertThat(line2).doesNotContain("She");
		}

		@ParameterizedTest
		@DisableIfAllArguments(contains = { "She", "she" })
		@MethodSource("requiescat")
		void interceptContainsAll(String line, String line2) {
		}

		@ParameterizedTest
		@DisableIfArgument(matches = { ".*[tr].?" })
		@MethodSource("requiescat")
		void interceptMatches(String line, String line2) {
			assertThat(line).doesNotMatch(".*[tr].?");
			assertThat(line2).doesNotMatch(".*[tr].?");
		}

		@ParameterizedTest
		@DisableIfAnyArgument(matches = ".*here?.*")
		@MethodSource("requiescat")
		void interceptMatchesAny(String line, String line2) {
			assertThat(line).doesNotContainPattern("here?");
			assertThat(line2).doesNotContainPattern("here?");
		}

		@ParameterizedTest
		@DisableIfAllArguments(matches = ".*[Ss]he.*")
		@MethodSource("requiescat")
		void interceptMatchesAll(String line, String line2) {
			assertThat(line).doesNotContainPattern(".*[Ss]he.*");
			assertThat(line2).doesNotContainPattern(".*[Ss]he.*");
		}

		@ParameterizedTest
		@DisableIfArgument(index = 1, contains = "she")
		@DisableIfArgument(index = 0, contains = "her")
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
		@DisableIfAnyArgument(contains = "A")
		void simpleTest() {
		}

		@ParameterizedTest
		@DisableIfArgument(index = 3, contains = "overindexed")
		@ValueSource(strings = { "a", "b" })
		void overindexed(String s) {
		}

		@ParameterizedTest
		@DisableIfArgument(index = 0, name = "s", contains = "value")
		@ValueSource(strings = { "value" })
		void multipleTargets(String s) {
		}

		@ParameterizedTest
		@DisableIfArgument(index = 0, contains = "aaa")
		@ValueSource(strings = { "aaa" })
		void noParameters() {
		}

		@ParameterizedTest
		@DisableIfAnyArgument(contains = "a")
		@ValueSource(strings = { "aaa" })
		void noParametersAny() {
		}

		@ParameterizedTest
		@DisableIfAllArguments(contains = "a")
		@ValueSource(strings = { "aaa" })
		void noParametersAll() {
		}

		@ParameterizedTest
		@DisableIfArgument
		@ValueSource(strings = { "A", "B", "C" })
		void missingInputs(String value) {
		}

		@ParameterizedTest
		@DisableIfAnyArgument
		@ValueSource(strings = { "A", "B", "C" })
		void missingInputsAny(String value) {
		}

		@ParameterizedTest
		@DisableIfAllArguments
		@ValueSource(strings = { "A", "B", "C" })
		void missingInputsAll(String value) {
		}

		@ParameterizedTest
		@DisableIfArgument(name = "param", contains = { "A" })
		@ValueSource(strings = { "A", "B", "C" })
		void badlyNamedParam(String value) {
		}

		@ParameterizedTest
		@DisableIfArgument(contains = { "sonnet", "life" }, matches = "^.*(Peace, )\\1.*$")
		@ValueSource(strings = { "A", "B", "C" })
		void setBothParams(String value) {
		}

		@ParameterizedTest
		@DisableIfAnyArgument(contains = { "sonnet", "life" }, matches = "^.*(Peace, )\\1.*$")
		@ValueSource(strings = { "A", "B", "C" })
		void setBothParamsAny(String value) {
		}

		@ParameterizedTest
		@DisableIfAllArguments(contains = { "sonnet", "life" }, matches = "^.*(Peace, )\\1.*$")
		@ValueSource(strings = { "A", "B", "C" })
		void setBothParamsAll(String value) {
		}

		@ParameterizedTest
		@ExtendWith(DisableIfArgumentExtension.class)
		@ValueSource(strings = { "a" })
		void forced(String s) {
		}

	}

}
