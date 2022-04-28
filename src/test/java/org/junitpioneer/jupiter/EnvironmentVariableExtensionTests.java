/*
 * Copyright 2016-2021 the original author or authors.
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
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junitpioneer.testkit.ExecutionResults;

@EnabledForJreRange(max = JRE.JAVA_16, disabledReason = "See: https://github.com/junit-pioneer/junit-pioneer/issues/509")
@DisplayName("EnvironmentVariable extension")
class EnvironmentVariableExtensionTests {

	@BeforeAll
	static void globalSetUp() {
		EnvironmentVariableUtils.set("set envvar A", "old A");
		EnvironmentVariableUtils.set("set envvar B", "old B");
		EnvironmentVariableUtils.set("set envvar C", "old C");

		EnvironmentVariableUtils.clear("clear envvar D");
		EnvironmentVariableUtils.clear("clear envvar E");
		EnvironmentVariableUtils.clear("clear envvar F");
	}

	@AfterAll
	static void globalTearDown() {
		assertThat(systemEnvironmentVariable("set envvar A")).isEqualTo("old A");
		EnvironmentVariableUtils.clear("set envvar A");
		assertThat(systemEnvironmentVariable("set envvar B")).isEqualTo("old B");
		EnvironmentVariableUtils.clear("set envvar B");
		assertThat(systemEnvironmentVariable("set envvar C")).isEqualTo("old C");
		EnvironmentVariableUtils.clear("set envvar C");

		assertThat(systemEnvironmentVariable("clear envvar D")).isNull();
		assertThat(systemEnvironmentVariable("clear envvar E")).isNull();
		assertThat(systemEnvironmentVariable("clear envvar F")).isNull();
	}

	private static String systemEnvironmentVariable(String variable) {
		return System.getenv(variable); //NOSONAR access required to implement the tests
	}

	@Nested
	@DisplayName("used with ClearEnvironmentVariable")
	@ClearEnvironmentVariable(key = "set envvar A")
	class ClearEnvironmentVariableTests {

		@Test
		@DisplayName("should clear environment variable")
		@ClearEnvironmentVariable(key = "set envvar B")
		void shouldClearEnvironmentVariable() {
			assertThat(systemEnvironmentVariable("set envvar A")).isNull();
			assertThat(systemEnvironmentVariable("set envvar B")).isNull();
			assertThat(systemEnvironmentVariable("set envvar C")).isEqualTo("old C");

			assertThat(systemEnvironmentVariable("clear envvar D")).isNull();
			assertThat(systemEnvironmentVariable("clear envvar E")).isNull();
			assertThat(systemEnvironmentVariable("clear envvar F")).isNull();
		}

		@Test
		@DisplayName("should be repeatable")
		@ClearEnvironmentVariable(key = "set envvar B")
		@ClearEnvironmentVariable(key = "set envvar C")
		void shouldBeRepeatable() {
			assertThat(systemEnvironmentVariable("set envvar A")).isNull();
			assertThat(systemEnvironmentVariable("set envvar B")).isNull();
			assertThat(systemEnvironmentVariable("set envvar C")).isNull();

			assertThat(systemEnvironmentVariable("clear envvar D")).isNull();
			assertThat(systemEnvironmentVariable("clear envvar E")).isNull();
			assertThat(systemEnvironmentVariable("clear envvar F")).isNull();
		}

	}

	@Nested
	@DisplayName("used with SetEnvironmentVariable")
	@SetEnvironmentVariable(key = "set envvar A", value = "new A")
	class SetEnvironmentVariableTests {

		@Test
		@DisplayName("should set environment variable to value")
		@SetEnvironmentVariable(key = "set envvar B", value = "new B")
		void shouldSetEnvironmentVariableToValue() {
			assertThat(systemEnvironmentVariable("set envvar A")).isEqualTo("new A");
			assertThat(systemEnvironmentVariable("set envvar B")).isEqualTo("new B");
			assertThat(systemEnvironmentVariable("set envvar C")).isEqualTo("old C");

			assertThat(systemEnvironmentVariable("clear envvar D")).isNull();
			assertThat(systemEnvironmentVariable("clear envvar E")).isNull();
			assertThat(systemEnvironmentVariable("clear envvar F")).isNull();
		}

		@Test
		@DisplayName("should be repeatable")
		@SetEnvironmentVariable(key = "set envvar B", value = "new B")
		@SetEnvironmentVariable(key = "clear envvar D", value = "new D")
		void shouldBeRepeatable() {
			assertThat(systemEnvironmentVariable("set envvar A")).isEqualTo("new A");
			assertThat(systemEnvironmentVariable("set envvar B")).isEqualTo("new B");
			assertThat(systemEnvironmentVariable("set envvar C")).isEqualTo("old C");

			assertThat(systemEnvironmentVariable("clear envvar D")).isEqualTo("new D");
			assertThat(systemEnvironmentVariable("clear envvar E")).isNull();
			assertThat(systemEnvironmentVariable("clear envvar F")).isNull();
		}

	}

	@Nested
	@DisplayName("used with both ClearEnvironmentVariable and SetEnvironmentVariable")
	@ClearEnvironmentVariable(key = "set envvar A")
	@SetEnvironmentVariable(key = "clear envvar D", value = "new D")
	class CombinedEnvironmentVariableTests {

		@Test
		@DisplayName("should be combinable")
		@ClearEnvironmentVariable(key = "set envvar B")
		@SetEnvironmentVariable(key = "clear envvar E", value = "new E")
		void clearAndSetEnvironmentVariableShouldBeCombinable() {
			assertThat(systemEnvironmentVariable("set envvar A")).isNull();
			assertThat(systemEnvironmentVariable("set envvar B")).isNull();
			assertThat(systemEnvironmentVariable("set envvar C")).isEqualTo("old C");

			assertThat(systemEnvironmentVariable("clear envvar D")).isEqualTo("new D");
			assertThat(systemEnvironmentVariable("clear envvar E")).isEqualTo("new E");
			assertThat(systemEnvironmentVariable("clear envvar F")).isNull();
		}

		@Test
		@DisplayName("method level should overwrite class level")
		@ClearEnvironmentVariable(key = "clear envvar D")
		@SetEnvironmentVariable(key = "set envvar A", value = "new A")
		void methodLevelShouldOverwriteClassLevel() {
			assertThat(systemEnvironmentVariable("set envvar A")).isEqualTo("new A");
			assertThat(systemEnvironmentVariable("set envvar B")).isEqualTo("old B");
			assertThat(systemEnvironmentVariable("set envvar C")).isEqualTo("old C");

			assertThat(systemEnvironmentVariable("clear envvar D")).isNull();
			assertThat(systemEnvironmentVariable("clear envvar E")).isNull();
			assertThat(systemEnvironmentVariable("clear envvar F")).isNull();
		}

		@Test
		@Issue("473")
		@DisplayName("method level should not clash (in terms of duplicate entries) with class level")
		@SetEnvironmentVariable(key = "set envvar A", value = "new A")
		void methodLevelShouldNotClashWithClassLevel() {
			assertThat(systemEnvironmentVariable("set envvar A")).isEqualTo("new A");
			assertThat(systemEnvironmentVariable("set envvar B")).isEqualTo("old B");
			assertThat(systemEnvironmentVariable("set envvar C")).isEqualTo("old C");
			assertThat(systemEnvironmentVariable("clear envvar D")).isEqualTo("new D");

			assertThat(systemEnvironmentVariable("clear envvar E")).isNull();
			assertThat(systemEnvironmentVariable("clear envvar F")).isNull();
		}

	}

	@DisplayName("with nested classes")
	@ClearEnvironmentVariable(key = "set envvar A")
	@SetEnvironmentVariable(key = "set envvar B", value = "new B")
	@Nested
	class NestedEnvironmentVariableTests {

		@Nested
		@TestMethodOrder(OrderAnnotation.class)
		@DisplayName("without EnvironmentVariable annotations")
		class NestedClass {

			@Test
			@Order(1)
			@ReadsEnvironmentVariable
			@DisplayName("environment variables should be set from enclosed class when they are not provided in nested")
			void shouldSetEnvironmentVariableFromEnclosedClass() {
				assertThat(systemEnvironmentVariable("set envvar A")).isNull();
				assertThat(systemEnvironmentVariable("set envvar B")).isEqualTo("new B");
			}

			@Test
			@Issue("480")
			@Order(2)
			@ReadsEnvironmentVariable
			@DisplayName("environment variables should be set from enclosed class after restore")
			void shouldSetEnvironmentVariableFromEnclosedClassAfterRestore() {
				assertThat(systemEnvironmentVariable("set envvar A")).isNull();
				assertThat(systemEnvironmentVariable("set envvar B")).isEqualTo("new B");
			}

		}

		@Nested
		@SetEnvironmentVariable(key = "set envvar B", value = "newer B")
		@DisplayName("with SetEnvironmentVariable annotation")
		class AnnotatedNestedClass {

			@Test
			@ReadsEnvironmentVariable
			@DisplayName("environment variable should be set from nested class when it is provided")
			void shouldSetEnvironmentVariableFromNestedClass() {
				assertThat(systemEnvironmentVariable("set envvar B")).isEqualTo("newer B");
			}

			@Test
			@SetEnvironmentVariable(key = "set envvar B", value = "newest B")
			@DisplayName("environment variable should be set from method when it is provided")
			void shouldSetEnvironmentVariableFromMethodOfNestedClass() {
				assertThat(systemEnvironmentVariable("set envvar B")).isEqualTo("newest B");
			}

		}

	}

	@Nested
	@SetEnvironmentVariable(key = "set envvar A", value = "new A")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class ResettingEnvironmentVariableTests {

		@Nested
		@SetEnvironmentVariable(key = "set envvar A", value = "newer A")
		@TestInstance(TestInstance.Lifecycle.PER_CLASS)
		class ResettingEnvironmentVariableNestedTests {

			@Test
			@SetEnvironmentVariable(key = "set envvar A", value = "newest A")
			void setForTestMethod() {
				assertThat(System.getenv("set envvar A")).isEqualTo("newest A");
			}

			@AfterAll
			@ReadsEnvironmentVariable
			void resetAfterTestMethodExecution() {
				assertThat(System.getenv("set envvar A")).isEqualTo("old A");
			}

		}

		@AfterAll
		@ReadsEnvironmentVariable
		void resetAfterTestContainerExecution() {
			assertThat(System.getenv("set envvar A")).isEqualTo("old A");
		}

	}

	@Nested
	@DisplayName("used with incorrect configuration")
	class ConfigurationFailureTests {

		@Test
		@DisplayName("should fail when clear and set same environment variable")
		void shouldFailWhenClearAndSetSameEnvironmentVariable() {
			ExecutionResults results = executeTestMethod(MethodLevelInitializationFailureTestCases.class,
				"shouldFailWhenClearAndSetSameEnvironmentVariable");

			assertThat(results).hasSingleFailedTest().withExceptionInstanceOf(ExtensionConfigurationException.class);
		}

		@Test
		@DisplayName("should fail when clear same environment variable twice")
		@Disabled("This can't happen at the moment, because Jupiter's annotation tooling "
				+ "deduplicates identical annotations like the ones required for this test: "
				+ "https://github.com/junit-team/junit5/issues/2131")
		void shouldFailWhenClearSameEnvironmentVariableTwice() {
			ExecutionResults results = executeTestMethod(MethodLevelInitializationFailureTestCases.class,
				"shouldFailWhenClearSameEnvironmentVariableTwice");

			assertThat(results).hasSingleFailedTest().withExceptionInstanceOf(ExtensionConfigurationException.class);
		}

		@Test
		@DisplayName("should fail when set same environment variable twice")
		void shouldFailWhenSetSameEnvironmentVariableTwice() {
			ExecutionResults results = executeTestMethod(MethodLevelInitializationFailureTestCases.class,
				"shouldFailWhenSetSameEnvironmentVariableTwice");

			assertThat(results).hasSingleFailedTest().withExceptionInstanceOf(ExtensionConfigurationException.class);
		}

	}

	@Nested
	// These tests verify whether warnings are reported correctly. For the warnings to be
	// actually reported, `EnvironmentVariableExtension.REPORTED_WARNING` needs to be reset
	// to `false` before each test and no other test must run in parallel because it may
	// generate its own warning, thus setting the flag to `true`, preventing that these
	// tests here can report anything. To make sure, these tests are not run in parallel
	// with any other environment-variable-writing test, we apply the following annotation:
	@WritesEnvironmentVariable
	class ReportWarningTests {

		@BeforeEach
		void resetWarning() {
			EnvironmentVariableExtension.REPORTED_WARNING.set(false);
		}

		@Test
		@StdIo
		void shouldNotReportWarningIfExtensionNotUsed(StdOut out) {
			ExecutionResults results = executeTestMethod(ReportWarningTestCases.class, "testWithoutExtension");

			assertThat(results).hasNoReportEntries();
			assertThat(out.capturedLines()).containsExactly("");
		}

		@Test
		@StdIo
		void shouldReportWarningIfExtensionUsed(StdOut out) {
			ExecutionResults results = executeTestMethod(ReportWarningTestCases.class, "testWithExtension");

			assertThat(results).hasSingleReportEntry().withKeyAndValue(WARNING_KEY, WARNING_VALUE);
			assertThat(out.capturedLines()).containsExactly(WARNING_KEY + ": " + WARNING_VALUE);
		}

		@Test
		void shouldReportWarningExactlyOnce() {
			ExecutionResults results = executeTestClass(ReportWarningTestCases.class);

			assertThat(results).hasSingleReportEntry().withKeyAndValue(WARNING_KEY, WARNING_VALUE);
		}

	}

	static class ReportWarningTestCases {

		@Test
		void testWithoutExtension() {
		}

		@Test
		@ClearEnvironmentVariable(key = "set envvar A")
		void testWithExtension() {
		}

		@Test
		@ClearEnvironmentVariable(key = "set envvar A")
		void anotherTestWithExtension() {
		}

	}

	static class MethodLevelInitializationFailureTestCases {

		@Test
		@DisplayName("clearing and setting the same variable")
		@ClearEnvironmentVariable(key = "set envvar A")
		@SetEnvironmentVariable(key = "set envvar A", value = "new A")
		void shouldFailWhenClearAndSetSameEnvironmentVariable() {
		}

		@Test
		@ClearEnvironmentVariable(key = "set envvar A")
		@ClearEnvironmentVariable(key = "set envvar A")
		void shouldFailWhenClearSameEnvironmentVariableTwice() {
		}

		@Test
		@SetEnvironmentVariable(key = "set envvar A", value = "new A")
		@SetEnvironmentVariable(key = "set envvar A", value = "new B")
		void shouldFailWhenSetSameEnvironmentVariableTwice() {
		}

	}

	@Nested
	@DisplayName("used with inheritance")
	class InheritanceTests extends InheritanceBaseTest {

		@Test
		@Issue("448")
		@DisplayName("should inherit clear and set annotations")
		void shouldInheritClearAndSetProperty() {
			assertThat(systemEnvironmentVariable("set envvar A")).isNull();
			assertThat(systemEnvironmentVariable("set envvar B")).isNull();
			assertThat(systemEnvironmentVariable("clear envvar D")).isEqualTo("new D");
			assertThat(systemEnvironmentVariable("clear envvar E")).isEqualTo("new E");
		}

	}

	@ClearEnvironmentVariable(key = "set envvar A")
	@ClearEnvironmentVariable(key = "set envvar B")
	@SetEnvironmentVariable(key = "clear envvar D", value = "new D")
	@SetEnvironmentVariable(key = "clear envvar E", value = "new E")
	static class InheritanceBaseTest {

	}

}
