/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.displaynamegenerator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ReplaceCamelCaseAndUnderscoreAndNumberDemo {

	// tag::class_using_replace_camel_case_and_underscore_and_number_generator[]

	@DisplayNameGeneration(ReplaceCamelCaseAndUnderscoreAndNumber.class)
	class ReplaceCamelCaseAndUnderscoreAndNumberStyleTest {
		// tag::shouldReturnErrorWhen_maxResults_IsNegative[]
		@Test
		void shouldReturnErrorWhen_maxResults_IsNegative() {}
		// end::shouldReturnErrorWhen_maxResults_IsNegative[]

		// tag::shouldCreateLimitWithRange[]
		@ParameterizedTest
		@ValueSource(strings = {"", "  "})
		void shouldCreateLimitWithRange(String input) {
			methodNotAnnotatedWithTestOrParameterizedTest();
		}
		// end::shouldCreateLimitWithRange[]

		// tag::shouldReturn5Errors[]
		@ParameterizedTest
		@ValueSource(ints = {5, 23})
		void shouldReturn5Errors(int input) {}
		// end::shouldReturn5Errors[]

		// tag::shouldReturn5errors_no_params[]
		@Test
		void shouldReturn5errors() {
			methodNotAnnotatedWithTestOrParameterizedTest();
		}
		// end::shouldReturn5errors_no_params[]

		// tag::shouldReturn23Errors[]
		@Test
		void shouldReturn23Errors() {
			methodNotAnnotatedWithTestOrParameterizedTest();
		}
		// end::shouldReturn23Errors[]

		// tag::shouldReturnTheValueOf_maxResults[]
		@Test
		void shouldReturnTheValueOf_maxResults() {
			methodNotAnnotatedWithTestOrParameterizedTest();
		}
		// end::shouldReturnTheValueOf_maxResults[]

		// tag::shouldReturnTheNumberOfErrorsAs_numberOfErrors_InferiorOrEqualTo5[]
		@ParameterizedTest
		@ValueSource(strings = {"", "  "})
		void shouldReturnTheNumberOfErrorsAs_numberOfErrors_InferiorOrEqualTo5(String input) {}
		// end::shouldReturnTheNumberOfErrorsAs_numberOfErrors_InferiorOrEqualTo5[]

		// tag::shouldReturnTheNumberOfErrorsAs_numberOfErrors_InferiorOrEqualTo15[]
		@Test
		void shouldReturnTheNumberOfErrorsAs_numberOfErrors_InferiorOrEqualTo15() {}
		// end::shouldReturnTheNumberOfErrorsAs_numberOfErrors_InferiorOrEqualTo15[]

		// tag::methodNotAnnotatedWithTestOrParameterizedTest[]
		private void methodNotAnnotatedWithTestOrParameterizedTest() {}
		// end::methodNotAnnotatedWithTestOrParameterizedTest[]

		// tag::testDisplayNamePrevails[]
		@DisplayName("@DisplayName prevails")
		@Test
		void testDisplayNamePrevails() {}
		// end::testDisplayNamePrevails[]

	}

	// end::class_using_replace_camel_case_and_underscore_and_number_generator[]
}
