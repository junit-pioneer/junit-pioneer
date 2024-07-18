/*
 * Copyright 2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.displaynamegenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.engine.TestDescriptor;

/**
 * Testing {@linkplain ReplaceCamelCaseAndUnderscoreAndNumber} by extending this custom engine {@linkplain ReplaceCamelCaseAndUnderscoreAndNumberTestEngine}
 * instead of {@linkplain org.junit.jupiter.engine.JupiterTestEngine } because it is internal to JUnit
 * */

class ReplaceCamelCaseAndUnderscoreAndNumberTests extends ReplaceCamelCaseAndUnderscoreAndNumberTestEngine {

	@Test
	void replaceCamelCaseAndUnderscoreAndNumberGenerator() {
		check(
			"CONTAINER: ReplaceCamelCaseAndUnderscoreAndNumberTests$ReplaceCamelCaseAndUnderscoreAndNumberStyleTestCase",
			"TEST: @DisplayName prevails", "TEST: Should return error when maxResults is negative",
			"TEST: Should create limit with range (String)", "TEST: Should return 5 errors (int)",
			"TEST: Should return 5errors", "TEST: Should return 23 errors",
			"TEST: Should return the value of maxResults",
			"TEST: Should return the number of errors as numberOfErrors inferior or equal to 5 (String)",
			"TEST: Should return the number of errors as numberOfErrors inferior or equal to 15");

	}

	private void check(String... expectedDisplayNames) {
		var request = request()
				.selectors(selectClass(ReplaceCamelCaseAndUnderscoreAndNumberStyleTestCase.class))
				.build();
		var descriptors = discoverTests(request).getDescendants();
		assertThat(descriptors).map(this::describe).containsExactlyInAnyOrder(expectedDisplayNames);
	}

	private String describe(TestDescriptor descriptor) {
		return descriptor.getType() + ": " + descriptor.getDisplayName();
	}

	@DisplayNameGeneration(ReplaceCamelCaseAndUnderscoreAndNumber.class)
	static class ReplaceCamelCaseAndUnderscoreAndNumberStyleTestCase {

		@Test
		void shouldReturnErrorWhen_maxResults_IsNegative() {
		}

		@ParameterizedTest
		@ValueSource(strings = { "", "  " })
		void shouldCreateLimitWithRange(String input) {
		}

		@ParameterizedTest
		@ValueSource(ints = { 5, 23 })
		void shouldReturn5Errors(int input) {
		}

		@Test
		void shouldReturn5errors() {
		}

		@Test
		void shouldReturn23Errors() {
		}

		@Test
		void shouldReturnTheValueOf_maxResults() {
		}

		@ParameterizedTest
		@ValueSource(strings = { "", "  " })
		void shouldReturnTheNumberOfErrorsAs_numberOfErrors_InferiorOrEqualTo5(String input) {
		}

		@Test
		void shouldReturnTheNumberOfErrorsAs_numberOfErrors_InferiorOrEqualTo15() {
		}

		@DisplayName("@DisplayName prevails")
		@Test
		void testDisplayNamePrevails() {
		}

	}

}
