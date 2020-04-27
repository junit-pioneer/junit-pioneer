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
import static org.junitpioneer.jupiter.EnvironmentVariableExtension.WARNING_KEY;
import static org.junitpioneer.jupiter.EnvironmentVariableExtension.WARNING_VALUE;
import static org.junitpioneer.testkit.PioneerTestKit.executeTestClass;
import static org.junitpioneer.testkit.PioneerTestKit.executeTestMethod;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junitpioneer.testkit.ExecutionResults;

@DisplayName("EnvironmentVariable extension")
class EnvironmentVariableExtensionTests {

	@BeforeAll
	static void globalSetUp() {
		EnvironmentVariableUtils.set("set prop A", "old A");
		EnvironmentVariableUtils.set("set prop B", "old B");
		EnvironmentVariableUtils.set("set prop C", "old C");

		EnvironmentVariableUtils.clear("clear prop D");
		EnvironmentVariableUtils.clear("clear prop E");
		EnvironmentVariableUtils.clear("clear prop F");
	}

	@AfterAll
	static void globalTearDown() {
		assertThat(systemEnvironmentVariable("set prop A")).isEqualTo("old A");
		assertThat(systemEnvironmentVariable("set prop B")).isEqualTo("old B");
		assertThat(systemEnvironmentVariable("set prop C")).isEqualTo("old C");

		assertThat(systemEnvironmentVariable("clear prop D")).isNull();
		assertThat(systemEnvironmentVariable("clear prop E")).isNull();
		assertThat(systemEnvironmentVariable("clear prop F")).isNull();
	}

	private static String systemEnvironmentVariable(String variable) {
		return System.getenv(variable); //NOSONAR access required to implement the tests
	}

	@Nested
	@DisplayName("used with ClearEnvironmentVariable")
	@ClearEnvironmentVariable(key = "set prop A")
	class ClearEnvironmentVariableTests {

		@Test
		@DisplayName("should clear environment variable")
		@ClearEnvironmentVariable(key = "set prop B")
		void shouldClearEnvironmentVariable() {
			assertThat(systemEnvironmentVariable("set prop A")).isNull();
			assertThat(systemEnvironmentVariable("set prop B")).isNull();
			assertThat(systemEnvironmentVariable("set prop C")).isEqualTo("old C");

			assertThat(systemEnvironmentVariable("clear prop D")).isNull();
			assertThat(systemEnvironmentVariable("clear prop E")).isNull();
			assertThat(systemEnvironmentVariable("clear prop F")).isNull();
		}

		@Test
		@DisplayName("should be repeatable")
		@ClearEnvironmentVariable(key = "set prop B")
		@ClearEnvironmentVariable(key = "set prop C")
		void shouldBeRepeatable() {
			assertThat(systemEnvironmentVariable("set prop A")).isNull();
			assertThat(systemEnvironmentVariable("set prop B")).isNull();
			assertThat(systemEnvironmentVariable("set prop C")).isNull();

			assertThat(systemEnvironmentVariable("clear prop D")).isNull();
			assertThat(systemEnvironmentVariable("clear prop E")).isNull();
			assertThat(systemEnvironmentVariable("clear prop F")).isNull();
		}

	}

	@Nested
	@DisplayName("used with SetEnvironmentVariable")
	@SetEnvironmentVariable(key = "set prop A", value = "new A")
	class SetEnvironmentVariableTests {

		@Test
		@DisplayName("should set environment variable to value")
		@SetEnvironmentVariable(key = "set prop B", value = "new B")
		void shouldSetEnvironmentVariableToValue() {
			assertThat(systemEnvironmentVariable("set prop A")).isEqualTo("new A");
			assertThat(systemEnvironmentVariable("set prop B")).isEqualTo("new B");
			assertThat(systemEnvironmentVariable("set prop C")).isEqualTo("old C");

			assertThat(systemEnvironmentVariable("clear prop D")).isNull();
			assertThat(systemEnvironmentVariable("clear prop E")).isNull();
			assertThat(systemEnvironmentVariable("clear prop F")).isNull();
		}

		@Test
		@DisplayName("should be repeatable")
		@SetEnvironmentVariable(key = "set prop B", value = "new B")
		@SetEnvironmentVariable(key = "clear prop D", value = "new D")
		void shouldBeRepeatable() {
			assertThat(systemEnvironmentVariable("set prop A")).isEqualTo("new A");
			assertThat(systemEnvironmentVariable("set prop B")).isEqualTo("new B");
			assertThat(systemEnvironmentVariable("set prop C")).isEqualTo("old C");

			assertThat(systemEnvironmentVariable("clear prop D")).isEqualTo("new D");
			assertThat(systemEnvironmentVariable("clear prop E")).isNull();
			assertThat(systemEnvironmentVariable("clear prop F")).isNull();
		}

	}

	@Nested
	@DisplayName("used with both ClearEnvironmentVariable and SetEnvironmentVariable")
	@ClearEnvironmentVariable(key = "set prop A")
	@SetEnvironmentVariable(key = "clear prop D", value = "new D")
	class CombinedEnvironmentVariableTests {

		@Test
		@DisplayName("should be combinable")
		@ClearEnvironmentVariable(key = "set prop B")
		@SetEnvironmentVariable(key = "clear prop E", value = "new E")
		void clearAndSetEnvironmentVariableShouldBeCombinable() {
			assertThat(systemEnvironmentVariable("set prop A")).isNull();
			assertThat(systemEnvironmentVariable("set prop B")).isNull();
			assertThat(systemEnvironmentVariable("set prop C")).isEqualTo("old C");

			assertThat(systemEnvironmentVariable("clear prop D")).isEqualTo("new D");
			assertThat(systemEnvironmentVariable("clear prop E")).isEqualTo("new E");
			assertThat(systemEnvironmentVariable("clear prop F")).isNull();
		}

		@Test
		@DisplayName("method level should overwrite class level")
		@ClearEnvironmentVariable(key = "clear prop D")
		@SetEnvironmentVariable(key = "set prop A", value = "new A")
		void methodLevelShouldOverwriteClassLevel() {
			assertThat(systemEnvironmentVariable("set prop A")).isEqualTo("new A");
			assertThat(systemEnvironmentVariable("set prop B")).isEqualTo("old B");
			assertThat(systemEnvironmentVariable("set prop C")).isEqualTo("old C");

			assertThat(systemEnvironmentVariable("clear prop D")).isNull();
			assertThat(systemEnvironmentVariable("clear prop E")).isNull();
			assertThat(systemEnvironmentVariable("clear prop F")).isNull();
		}

	}

	@DisplayName("with nested classes")
	@ClearEnvironmentVariable(key = "set prop A")
	@SetEnvironmentVariable(key = "set prop B", value = "new B")
	@Nested
	class NestedEnvironmentVariableTests {

		@Nested
		@DisplayName("without EnvironmentVariable annotations")
		class NestedClass {

			@Test
			@DisplayName("environment variables should be set from enclosed class when they are not provided in nested")
			public void shouldSetEnvironmentVariableFromEnclosedClass() {
				assertThat(systemEnvironmentVariable("set prop A")).isNull();
				assertThat(systemEnvironmentVariable("set prop B")).isEqualTo("new B");
			}

		}

		@Nested
		@SetEnvironmentVariable(key = "set prop B", value = "newer B")
		@DisplayName("with SetEnvironmentVariable annotation")
		class AnnotatedNestedClass {

			@Test
			@DisplayName("environment variable should be set from nested class when it is provided")
			public void shouldSetEnvironmentVariableFromNestedClass() {
				assertThat(systemEnvironmentVariable("set prop B")).isEqualTo("newer B");
			}

			@Test
			@SetEnvironmentVariable(key = "set prop B", value = "newest B")
			@DisplayName("environment variable should be set from method when it is provided")
			public void shouldSetEnvironmentVariableFromMethodOfNestedClass() {
				assertThat(systemEnvironmentVariable("set prop B")).isEqualTo("newest B");
			}

		}

	}

	@Nested
	@DisplayName("used with incorrect configuration")
	class ConfigurationFailureTests {

		@Test
		@DisplayName("should fail when clear and set same environment variable")
		void shouldFailWhenClearAndSetSameEnvironmentVariable() {
			ExecutionResults results = executeTestMethod(MethodLevelInitializationFailureTestCase.class,
				"shouldFailWhenClearAndSetSameEnvironmentVariable");

			results.assertTestFailedWithExtensionConfigurationException();
		}

		@Test
		@DisplayName("should fail when clear same environment variable twice")
		@Disabled("This can't happen at the moment, because Jupiter's annotation tooling "
				+ "deduplicates identical annotations like the ones required for this test: "
				+ "https://github.com/junit-team/junit5/issues/2131")
		void shouldFailWhenClearSameEnvironmentVariableTwice() {
			ExecutionResults results = executeTestMethod(MethodLevelInitializationFailureTestCase.class,
				"shouldFailWhenClearSameEnvironmentVariableTwice");

			results.assertTestFailedWithExtensionConfigurationException();
		}

		@Test
		@DisplayName("should fail when set same environment variable twice")
		void shouldFailWhenSetSameEnvironmentVariableTwice() {
			ExecutionResults results = executeTestMethod(MethodLevelInitializationFailureTestCase.class,
				"shouldFailWhenSetSameEnvironmentVariableTwice");

			results.assertTestFailedWithExtensionConfigurationException();
		}

	}

	@Nested
	class ReportWarningTests {

		@BeforeEach
		void resetWarning() {
			EnvironmentVariableExtension.REPORTED_WARNING.set(false);
		}

		@Test
		void shouldNotReportWarningIfExtensionNotUsed() {
			ExecutionResults results = executeTestMethod(ReportWarningTestCases.class, "testWithoutExtension");

			assertThat(results.reportEntries()).hasSize(0);
		}

		@Test
		void shouldReportWarningIfExtensionUsed() {
			ExecutionResults results = executeTestMethod(ReportWarningTestCases.class, "testWithExtension");

			assertThat(results.singleReportEntry()).isEqualTo(TestUtils.entryOf(WARNING_KEY, WARNING_VALUE));
		}

		@Test
		void shouldReportWarningExactlyOnce() {
			ExecutionResults results = executeTestClass(ReportWarningTestCases.class);

			assertThat(results.reportEntries()).hasSize(1);
		}

	}

	static class ReportWarningTestCases {

		@Test
		void testWithoutExtension() {
		}

		@Test
		@ClearEnvironmentVariable(key = "set prop A")
		void testWithExtension() {
		}

		@Test
		@ClearEnvironmentVariable(key = "set prop A")
		void anotherTestWithExtension() {
		}

	}

	static class MethodLevelInitializationFailureTestCase {

		@Test
		@DisplayName("clearing and setting the same variable")
		@ClearEnvironmentVariable(key = "set prop A")
		@SetEnvironmentVariable(key = "set prop A", value = "new A")
		void shouldFailWhenClearAndSetSameEnvironmentVariable() {
		}

		@Test
		@ClearEnvironmentVariable(key = "set prop A")
		@ClearEnvironmentVariable(key = "set prop A")
		void shouldFailWhenClearSameEnvironmentVariableTwice() {
		}

		@Test
		@SetEnvironmentVariable(key = "set prop A", value = "new A")
		@SetEnvironmentVariable(key = "set prop A", value = "new B")
		void shouldFailWhenSetSameEnvironmentVariableTwice() {
		}

	}

}
