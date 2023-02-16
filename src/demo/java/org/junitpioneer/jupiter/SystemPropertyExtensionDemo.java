/*
 * Copyright 2016-2022 the original author or authors.
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.params.IntRangeSource;

public class SystemPropertyExtensionDemo {

	// tag::systemproperty_clear_simple[]
	@Test
	@ClearSystemProperty(key = "some property")
	void testClearingProperty() {
		assertThat(System.getProperty("some property")).isNull();
	}
	// end::systemproperty_clear_simple[]

	// tag::systemproperty_set_simple[]
	@Test
	@SetSystemProperty(key = "some property", value = "new value")
	void testSettingProperty() {
		assertThat(System.getProperty("some property")).isEqualTo("new value");
	}
	// end::systemproperty_set_simple[]

	// tag::systemproperty_using_set_and_clear[]
	@Test
	@ClearSystemProperty(key = "1st property")
	@ClearSystemProperty(key = "2nd property")
	@SetSystemProperty(key = "3rd property", value = "new value")
	void testClearingAndSettingProperty() {
		assertThat(System.getProperty("1st property")).isNull();
		assertThat(System.getProperty("2nd property")).isNull();
		assertThat(System.getProperty("3rd property")).isEqualTo("new value");
	}
	// end::systemproperty_using_set_and_clear[]

	// tag::systemproperty_using_at_class_level[]
	@ClearSystemProperty(key = "some property")
	class MySystemPropertyTest {

		@Test
		@SetSystemProperty(key = "some property", value = "new value")
		void clearedAtClasslevel() {
			assertThat(System.getProperty("some property")).isEqualTo("new value");
		}

	}
	// end::systemproperty_using_at_class_level[]

	// tag::systemproperty_restore_test[]
	@ParameterizedTest
	@ValueSource(strings = { "foo", "bar" })
	@RestoreSystemProperties
	void parameterizedTest(String value) {
		System.setProperty("some parameterized property", value);
		System.setProperty("some other dynamic property", "my code calculates somehow");
	}
	// end::systemproperty_restore_test[]

	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	// tag::systemproperty_restore_class_level[]
	@RestoreSystemProperties
	class MySystemPropertyRestoreTest {

		@BeforeAll
		public void beforeAll() {
			System.setProperty("A", "A value");
		}

		@BeforeEach
		public void beforeEach() {
			System.setProperty("B", "B value");
		}

		@Test
		void isolatedTest1() {
			System.setProperty("C", "C value");
		}

		@Test
		void isolatedTest2() {
			assertThat(System.getProperty("A")).isEqualTo("A value");
			assertThat(System.getProperty("B")).isEqualTo("B value");

			//Class-level @RestoreSystemProperties restores 'C' to original state
			assertThat(System.getProperty("C")).isNull();
		}

	}

	class SomeOtherTest {
		// Changes to A, B & C have been restored to their values prior to the above test
	}
	// end::systemproperty_restore_class_level[]

	// tag::systemproperty_method_combine_all_test[]
	@ParameterizedTest
	@IntRangeSource(from = 0, to = 10000, step = 500)
	@RestoreSystemProperties
	@SetSystemProperty(key = "DISABLE_CACHE", value = "TRUE")
	@ClearSystemProperty(key = "COPYWRITE_OVERLAY_TEXT")
	void imageGenerationTest(int imageSize) {
		System.setProperty("IMAGE_SIZE", String.valueOf(imageSize)); // Requires Restore

		// Test your image generation utility with the current System Properties
	}
	// end::systemproperty_method_combine_all_test[]

}
