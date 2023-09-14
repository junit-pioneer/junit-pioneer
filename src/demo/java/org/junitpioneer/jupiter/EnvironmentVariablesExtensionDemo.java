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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.params.IntRangeSource;

@EnabledForJreRange(max = JRE.JAVA_16, disabledReason = "See: https://github.com/junit-pioneer/junit-pioneer/issues/509")
public class EnvironmentVariablesExtensionDemo {

	// tag::environment_clear_simple[]
	@Test
	@ClearEnvironmentVariable(key = "some variable")
	void testClear() {
		assertThat(System.getenv("some variable")).isNull();
	}
	// end::environment_clear_simple[]

	// tag::environment_set_simple[]
	@Test
	@SetEnvironmentVariable(key = "some variable", value = "new value")
	void testSet() {
		assertThat(System.getenv("some variable")).isEqualTo("new value");
	}
	// end::environment_set_simple[]

	// tag::environment_using_set_and_clear[]
	@Test
	@ClearEnvironmentVariable(key = "1st variable")
	@ClearEnvironmentVariable(key = "2nd variable")
	@SetEnvironmentVariable(key = "3rd variable", value = "new value")
	void testClearAndSet() {
		assertThat(System.getenv("1st variable")).isNull();
		assertThat(System.getenv("2nd variable")).isNull();
		assertThat(System.getenv("3rd variable")).isEqualTo("new value");
	}
	// end::environment_using_set_and_clear[]

	// tag::environment_using_at_class_level[]
	@ClearEnvironmentVariable(key = "some variable")
	class MyEnvironmentVariableTest {

		@Test
		@SetEnvironmentVariable(key = "some variable", value = "new value")
		void clearedAtClasslevel() {
			assertThat(System.getenv("some variable")).isEqualTo("new value");
		}

	}
	// end::environment_using_at_class_level[]

	// tag::environment_method_restore_test[]
	@ParameterizedTest
	@ValueSource(strings = { "foo", "bar" })
	@RestoreEnvironmentVariables
	void parameterizedTest(String value) {
		setEnvVar("some parameterized property", value);
		setEnvVar("some other dynamic property", "my code calculates somehow");
	}
	// end::environment_method_restore_test[]

	@Nested
	@TestClassOrder(ClassOrderer.OrderAnnotation.class)
	class EnvironmentVariableRestoreExample {

		@Nested
		@Order(1)
		@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Allow non-static @BeforeAll
		// tag::environment_class_restore_setup[]
		@RestoreEnvironmentVariables
		class EnvironmentVarRestoreTest {

			@BeforeAll
			void beforeAll() {
				setEnvVar("A", "A value");
			}

			@BeforeEach
			void beforeEach() {
				setEnvVar("B", "B value");
			}

			@Test
			void isolatedTest1() {
				setEnvVar("C", "C value");
			}

			@Test
			void isolatedTest2() {
				assertThat(System.getenv("A")).isEqualTo("A value");
				assertThat(System.getenv("B")).isEqualTo("B value");

				// Class-level @RestoreEnvironmentVariables restores "C" to original state
				assertThat(System.getenv("C")).isNull();
			}

		}
		// end::environment_class_restore_setup[]

		@Nested
		@Order(2)
		// tag::environment_class_restore_isolated_class[]
		// A test class that runs later
		@ReadsEnvironmentVariable
		class SomeOtherTestClass {

			@Test
			void isolatedTest() {
				// Class-level @RestoreEnvironmentVariables restores all changes made in EnvironmentVarRestoreTest
				assertThat(System.getenv("A")).isNull();
				assertThat(System.getenv("B")).isNull();
				assertThat(System.getenv("C")).isNull();
			}

			// Changes to A, B, C have been restored to their values prior to the above test
		}

		// end::environment_class_restore_isolated_class[]
	}

	// tag::environment_method_combine_all_test[]
	@ParameterizedTest
	@IntRangeSource(from = 0, to = 10000, step = 500)
	@RestoreEnvironmentVariables
	@SetEnvironmentVariable(key = "DISABLE_CACHE", value = "TRUE")
	@ClearEnvironmentVariable(key = "COPYWRITE_OVERLAY_TEXT")
	void imageGenerationTest(int imageSize) {
		setEnvVar("IMAGE_SIZE", String.valueOf(imageSize)); // Requires restore

		// Test your image generation utility with the current environment variables
	}
	// end::environment_method_combine_all_test[]

	public static void setEnvVar(String name, String value) {
		EnvironmentVariableUtils.set(name, value);
	}

}
