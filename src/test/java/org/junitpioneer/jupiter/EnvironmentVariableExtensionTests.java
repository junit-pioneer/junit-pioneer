/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;
import static org.junitpioneer.jupiter.EnvironmentVariableExtension.WARNING_KEY;
import static org.junitpioneer.jupiter.EnvironmentVariableExtension.WARNING_VALUE;
import static org.junitpioneer.testkit.PioneerTestKit.executeTestClass;
import static org.junitpioneer.testkit.PioneerTestKit.executeTestMethod;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.parallel.Execution;
import org.junitpioneer.testkit.ExecutionResults;

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
		EnvironmentVariableUtils.clear("set envvar A");
		EnvironmentVariableUtils.clear("set envvar B");
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
	class CombinedClearAndSetTests {

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

	@Nested
	@DisplayName("used with Clear, Set and Restore")
	@Execution(SAME_THREAD) // Uses instance state
	@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Uses instance state
	@TestClassOrder(ClassOrderer.OrderAnnotation.class)
	@WritesEnvironmentVariable // Many of these tests write, many also access
	class CombinedClearSetRestoreTests {

		Map<String, String> initialState;

		@BeforeAll
		void beforeAll() {
			HashMap<String, String> envVars = new HashMap<>();
			envVars.putAll(System.getenv()); //detached

			initialState = envVars;
		}

		@Nested
		@Order(1)
		@DisplayName("Set, Clear & Restore on class")
		@ClearEnvironmentVariable(key = "set envvar A")
		@SetEnvironmentVariable(key = "clear envvar D", value = "new D")
		@RestoreEnvironmentVariables
		@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
		@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Uses instance state
		class SetClearRestoreOnClass {

			@AfterAll // Can we restore from even this??
			void afterAll() {
				EnvironmentVariableUtils.set("XXX", "XXX Value");
			}

			@Test
			@Order(1)
			@DisplayName("Set, Clear on method w/ direct set Env Var")
			@ClearEnvironmentVariable(key = "set envvar B")
			@SetEnvironmentVariable(key = "clear envvar E", value = "new E")
			void clearSetRestoreShouldBeCombinable() {
				// Direct modification - shouldn't be visible in the next test
				EnvironmentVariableUtils.set("Restore", "Restore Me");

				assertThat(systemEnvironmentVariable("Restore")).isEqualTo("Restore Me");
				assertThat(systemEnvironmentVariable("set envvar A")).isNull();
				assertThat(systemEnvironmentVariable("set envvar B")).isNull();
				assertThat(systemEnvironmentVariable("set envvar C")).isEqualTo("old C");

				assertThat(systemEnvironmentVariable("clear envvar D")).isEqualTo("new D");
				assertThat(systemEnvironmentVariable("clear envvar E")).isEqualTo("new E");
				assertThat(systemEnvironmentVariable("clear envvar F")).isNull();
			}

			@Test
			@Order(2)
			@DisplayName("Restore from class should restore direct mods")
			void restoreShouldHaveRevertedDirectModification() {
				assertThat(systemEnvironmentVariable("Restore")).isNull();
			}

		}

		@Nested
		@Order(2)
		@DisplayName("Prior nested class changes should be restored}")
		class PriorNestedChangesRestored {

			@Test
			@DisplayName("Restore from class should restore direct mods")
			void restoreShouldHaveRevertedDirectModification() {
				// Set in SetClearRestoreOnClass
				assertThat(systemEnvironmentVariable("XXX")).isNull();

				assertThat(System.getenv()).containsExactlyInAnyOrderEntriesOf(initialState);
			}

		}

		@Nested
		@Order(3)
		@DisplayName("Set & Clear on class, Restore on method")
		@ClearEnvironmentVariable(key = "set envvar A")
		@SetEnvironmentVariable(key = "clear envvar D", value = "new D")
		@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
		@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Uses instance state
		class SetAndClearOnClass {

			Map<String, String> initialState;

			@BeforeAll
			void beforeAll() {
				HashMap<String, String> envVars = new HashMap<>();
				envVars.putAll(System.getenv()); //detached

				initialState = envVars;
			}

			@Test
			@Order(1)
			@DisplayName("Set, Clear & Restore on method w/ direct set Env Var")
			@ClearEnvironmentVariable(key = "set envvar B")
			@SetEnvironmentVariable(key = "clear envvar E", value = "new E")
			@RestoreEnvironmentVariables
			void clearSetRestoreShouldBeCombinable() {
				// Direct modification - shouldn't be visible in the next test
				EnvironmentVariableUtils.set("Restore", "Restore Me");

				assertThat(systemEnvironmentVariable("Restore")).isEqualTo("Restore Me");
				assertThat(systemEnvironmentVariable("set envvar A")).isNull();
				assertThat(systemEnvironmentVariable("set envvar B")).isNull();
				assertThat(systemEnvironmentVariable("set envvar C")).isEqualTo("old C");

				assertThat(systemEnvironmentVariable("clear envvar D")).isEqualTo("new D");
				assertThat(systemEnvironmentVariable("clear envvar E")).isEqualTo("new E");
				assertThat(systemEnvironmentVariable("clear envvar F")).isNull();
			}

			@Test
			@Order(2)
			@DisplayName("Restore from prior method should restore direct mods")
			void restoreShouldHaveRevertedDirectModification() {
				assertThat(systemEnvironmentVariable("Restore")).isNull();
				assertThat(System.getenv()).containsExactlyInAnyOrderEntriesOf(initialState);
			}

		}

	}

	@Nested
	@DisplayName("RestoreEnvironmentVariables individual methods tests")
	@WritesEnvironmentVariable // Many of these tests write, many also access
	class RestoreEnvironmentVariablesUnitTests {

		EnvironmentVariableExtension eve;

		@BeforeEach
		void beforeEach() {
			eve = new EnvironmentVariableExtension();
		}

		@Nested
		@DisplayName("Attributes of RestoreEnvironmentVariables")
		class BasicAttributesOfRestoreEnvironmentVariables {

			@Test
			@DisplayName("Restore annotation has correct markers")
			void restoreHasCorrectMarkers() {
				assertThat(RestoreEnvironmentVariables.class)
						.hasAnnotations(Inherited.class, WritesEnvironmentVariable.class);
			}

			@Test
			@DisplayName("Restore annotation has correct retention")
			void restoreHasCorrectRetention() {
				assertThat(RestoreEnvironmentVariables.class.getAnnotation(Retention.class).value())
						.isEqualTo(RetentionPolicy.RUNTIME);
			}

			@Test
			@DisplayName("Restore annotation has correct targets")
			void restoreHasCorrectTargets() {
				assertThat(RestoreEnvironmentVariables.class.getAnnotation(Target.class).value())
						.containsExactlyInAnyOrder(ElementType.METHOD, ElementType.TYPE);
			}

		}

		@Nested
		@DisplayName("RestorableContext Workflow Tests")
		class RestorableContextWorkflowTests {

			@Test
			@DisplayName("Workflow of RestorableContext")
			void workflowOfRestorableContexts() {
				Properties initialEnvVars = new Properties();
				initialEnvVars.putAll(System.getenv());

				try {
					Properties returnedFromPrepareToEnter = eve.prepareToEnterRestorableContext();

					assertThat(returnedFromPrepareToEnter).isStrictlyEqualTo(initialEnvVars);

					// Modify actual env vars
					EnvironmentVariableUtils.clear("set envvar A");
					EnvironmentVariableUtils.set("set envvar B", "XXX");
					EnvironmentVariableUtils.set("NewEntry", "I am new");

					// Sanity check: Above changes should be visible in env vars
					assertThat(System.getenv("set envvar A")).isNull();
					assertThat(System.getenv("set envvar B")).isEqualTo("XXX");
					assertThat(System.getenv("NewEntry")).isEqualTo("I am new");

					// Changes should not be reflected in detached clone
					Assertions.assertThat(returnedFromPrepareToEnter).contains(entry("set envvar A", "old A"));
					Assertions.assertThat(returnedFromPrepareToEnter).contains(entry("set envvar B", "old B"));
					Assertions.assertThat(returnedFromPrepareToEnter).doesNotContainKey("NewEntry");

					// Prepare to exit should restore original values
					eve.prepareToExitRestorableContext(returnedFromPrepareToEnter);

					// Verify changed vals
					assertThat(System.getenv("set envvar A")).isEqualTo("old A");
					assertThat(System.getenv("set envvar B")).isEqualTo("old B");
					assertThat(System.getenv("NewEntry")).isNull();

				}
				finally {
					// Failsafe: manually reset the values set in this method
					EnvironmentVariableUtils.clear("NewEntry");
				}
			}

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
		class ResettingEnvironmentVariableAfterEachNestedTests {

			@BeforeEach
			@ReadsEnvironmentVariable
			void changeShouldBeVisible() {
				// We already see "newest A" because BeforeEachCallBack is invoked before @BeforeEach
				// See https://junit.org/junit5/docs/current/user-guide/#extensions-execution-order-overview
				assertThat(System.getenv("set envvar A")).isEqualTo("newest A");
			}

			@Test
			@SetEnvironmentVariable(key = "set envvar A", value = "newest A")
			void setForTestMethod() {
				assertThat(System.getenv("set envvar A")).isEqualTo("newest A");
			}

			@AfterEach
			@ReadsEnvironmentVariable
			void resetAfterTestMethodExecution() {
				// We still see "newest A" because AfterEachCallBack is invoked after @AfterEach
				// See https://junit.org/junit5/docs/current/user-guide/#extensions-execution-order-overview
				assertThat(System.getenv("set envvar A")).isEqualTo("newest A");
			}

		}

		@Nested
		@SetEnvironmentVariable(key = "set envvar A", value = "newer A")
		@TestInstance(TestInstance.Lifecycle.PER_CLASS)
		class ResettingEnvironmentVariableAfterAllNestedTests {

			@BeforeAll
			@ReadsEnvironmentVariable
			void changeShouldBeVisible() {
				assertThat(System.getenv("set envvar A")).isEqualTo("newer A");
			}

			@Test
			@SetEnvironmentVariable(key = "set envvar A", value = "newest A")
			void setForTestMethod() {
				assertThat(System.getenv("set envvar A")).isEqualTo("newest A");
			}

			@AfterAll
			@ReadsEnvironmentVariable
			void resetAfterTestMethodExecution() {
				assertThat(System.getenv("set envvar A")).isEqualTo("newer A");
			}

		}

		@AfterAll
		@ReadsEnvironmentVariable
		void resetAfterTestContainerExecution() {
			assertThat(System.getenv("set envvar A")).isEqualTo("new A");
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
			assertThat(out.capturedLines()).isEmpty();
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

	@Nested
	@DisplayName("used with inheritance")
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@TestClassOrder(ClassOrderer.OrderAnnotation.class)
	@Execution(SAME_THREAD) // Uses instance state
	@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Uses instance state
	class InheritanceClearSetRestoreTests extends InheritanceClearSetRestoreBaseTest {

		Map<String, String> initialState;

		@BeforeAll
		void beforeAll() {
			HashMap<String, String> envVars = new HashMap<>();
			envVars.putAll(System.getenv()); // Detached

			initialState = envVars;
		}

		@Test
		@Order(1)
		@DisplayName("should inherit clear and set annotations")
		void shouldInheritClearSetRestore() {
			// Direct modification - shouldn't be visible in the next test
			EnvironmentVariableUtils.set("Restore", "Restore Me");

			assertThat(systemEnvironmentVariable("set envvar A")).isNull(); // The rest are checked elsewhere
		}

		@Test
		@Order(2)
		@DisplayName("Restore from class should restore direct mods")
		void restoreShouldHaveRevertedDirectModification() {
			assertThat(systemEnvironmentVariable("Restore")).isNull();
			assertThat(System.getenv()).containsExactlyInAnyOrderEntriesOf(initialState);
		}

		@Nested
		@Order(1)
		@DisplayName("Set props to ensure inherited restore")
		@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
		@TestInstance(TestInstance.Lifecycle.PER_CLASS)
		class SetSomeValuesToRestore {

			@AfterAll
			void afterAll() {
				EnvironmentVariableUtils.set("RestoreAll", "Restore Me"); // This should also be restored
			}

			@Test
			@Order(1)
			@DisplayName("Inherit values and restore behavior")
			void shouldInheritInNestedClass() {
				assertThat(systemEnvironmentVariable("set envvar A")).isNull();

				// Shouldn't be visible in the next test
				EnvironmentVariableUtils.set("Restore", "Restore Me");
			}

			@Test
			@Order(2)
			@DisplayName("Verify restore behavior bt methods")
			void verifyRestoreBetweenMethods() {
				assertThat(systemEnvironmentVariable("Restore")).isNull();
			}

		}

		@Nested
		@Order(2)
		@DisplayName("Verify env vars are restored")
		@TestInstance(TestInstance.Lifecycle.PER_CLASS)
		class VerifyValuesAreRestored {

			@Test
			@DisplayName("Inherit values and restore behavior")
			void shouldInheritInNestedClass() {
				assertThat(systemEnvironmentVariable("RestoreAll")).isNull(); // Should be restored
			}

		}

	}

	@ClearEnvironmentVariable(key = "set envvar A")
	@ClearEnvironmentVariable(key = "set envvar B")
	@SetEnvironmentVariable(key = "clear envvar D", value = "new D")
	@SetEnvironmentVariable(key = "clear envvar E", value = "new E")
	static class InheritanceBaseTest {

	}

	@ClearEnvironmentVariable(key = "set envvar A")
	@ClearEnvironmentVariable(key = "set envvar B")
	@SetEnvironmentVariable(key = "clear envvar D", value = "new D")
	@SetEnvironmentVariable(key = "clear envvar E", value = "new E")
	@RestoreEnvironmentVariables
	static class InheritanceClearSetRestoreBaseTest {
	}

}
