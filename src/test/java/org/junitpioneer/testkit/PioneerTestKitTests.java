/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.testkit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class PioneerTestKitTests {

	@DisplayName("Execute")
	@Nested
	class ExecuteTestTests {

		@DisplayName("all tests of a class")
		@Test
		void executeTestClass() {
			ExecutionResults results = PioneerTestKit.executeTestClass(DummyClass.class);

			assertThat(results).hasNumberOfStartedTests(1);
		}

		@DisplayName("a specific method")
		@Test
		void executeTestMethod() {
			ExecutionResults results = PioneerTestKit.executeTestMethod(DummyClass.class, "nothing");

			assertThat(results).hasNumberOfStartedTests(1);
		}

		@DisplayName("a specific parametrized method")
		@Nested
		class ExecuteTestMethodWithParametersTests {

			@DisplayName(" where parameter is a single class")
			@Test
			void executeTestMethodWithParameterTypesSingleParameterType() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethodWithParameterTypes(DummyParameterClass.class, "single", String.class);

				assertThat(results).hasNumberOfStartedTests(1);
			}

			@DisplayName(" where parameter is an array of classes")
			@Test
			void executeTestMethodWithParameterTypesParameterTypeAsArray() {
				Class<?>[] classes = { String.class };

				ExecutionResults results = PioneerTestKit
						.executeTestMethodWithParameterTypes(DummyParameterClass.class, "single", classes);

				assertThat(results).hasNumberOfStartedTests(1);
			}

			@DisplayName("without parameter results in IllegalArgumentException")
			@Test
			void executeTestMethodWithParameterTypesSingleParameterTypeIllegalArgumentExceptionIfParameterIsNull() {

				Throwable thrown = assertThrows(Throwable.class, () -> {
					PioneerTestKit.executeTestMethodWithParameterTypes(DummyParameterClass.class, "single", null);
				});

				String expectedMessage = "methodParameterTypes must not be null";

				Assertions.assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
				Assertions.assertThat(thrown.getMessage()).isEqualTo(expectedMessage);

			}

		}

	}

	static class DummyParameterClass {

		@ParameterizedTest(name = "See if enabled with {0}")
		@ValueSource(strings = { "parameter" })
		void single(String reason) {
			// Do nothing
		}

	}

	static class DummyClass {

		@Test
		void nothing() {
			// Do nothing
		}

	}

}
