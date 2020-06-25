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
import static org.assertj.core.api.Assertions.fail;
import static org.junitpioneer.testkit.PioneerTestKit.executeTestClass;
import static org.junitpioneer.testkit.PioneerTestKit.executeTestMethod;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.util.TimeZone;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junitpioneer.testkit.ExecutionResults;

@DisplayName("DefaultTimeZone extension")
class DefaultTimeZoneTests {

	private static TimeZone TEST_DEFAULT_TIMEZONE;
	private static TimeZone DEFAULT_TIMEZONE_BEFORE_TEST;

	@BeforeAll
	static void globalSetUp() {
		// the extension sets UTC as test time zone unless it is already
		// the system's time zone; in that case it uses GMT
		DEFAULT_TIMEZONE_BEFORE_TEST = TimeZone.getDefault();
		TimeZone utc = TimeZone.getTimeZone("UTC");
		TimeZone gmt = TimeZone.getTimeZone("GMT");
		if (DEFAULT_TIMEZONE_BEFORE_TEST.equals(utc)) {
			TimeZone.setDefault(gmt);
		} else {
			TimeZone.setDefault(utc);
		}
		TEST_DEFAULT_TIMEZONE = TimeZone.getDefault();
	}

	@AfterAll
	static void globalTearDown() {
		TimeZone.setDefault(DEFAULT_TIMEZONE_BEFORE_TEST);
	}

	@Nested
	@DisplayName("when applied on the method level")
	class MethodLevelTests {

		@Test
		@DisplayName("does nothing when annotation is not present")
		void doesNothingWhenAnnotationNotPresent() {
			assertThat(TimeZone.getDefault()).isEqualTo(TEST_DEFAULT_TIMEZONE);
		}

		@Test
		@DisplayName("throws exception on bad configuration")
		void throwsWhenConfigurationIsBad() {
			ExecutionResults results = executeTestMethod(BadMethodLevelConfigurationTestCase.class, "badConfiguration");

			assertThat(results)
					.hasSingleFailedTest()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessageNotContaining("should never execute")
					.hasMessageContaining("@DefaultTimeZone not configured correctly.");
		}

		@DefaultTimeZone("GMT")
		@Test
		@DisplayName("does not throw when explicitly set to GMT")
		void doesNotThrowForExplicitGmt() {
			assertThat(TimeZone.getDefault()).isEqualTo(TimeZone.getTimeZone("GMT"));
		}

		@DefaultTimeZone("CET")
		@Test
		@DisplayName("sets the default time zone using an abbreviation")
		void setsTimeZoneFromAbbreviation() {
			assertThat(TimeZone.getDefault()).isEqualTo(TimeZone.getTimeZone("CET"));
		}

		@DefaultTimeZone("America/Los_Angeles")
		@Test
		@DisplayName("sets the default time zone using a full name")
		void setsTimeZoneFromFullName() {
			assertThat(TimeZone.getDefault()).isEqualTo(TimeZone.getTimeZone("America/Los_Angeles"));
		}

	}

	@Nested
	@DisplayName("applied on the class level")
	class ClassLevelTests {

		@BeforeEach
		void setUp() {
			assertThat(TimeZone.getDefault()).isEqualTo(TEST_DEFAULT_TIMEZONE);
		}

		@Test
		@DisplayName("executes tests with configured TimeZone")
		void shouldExecuteTestsWithConfiguredTimeZone() {
			ExecutionResults results = executeTestClass(ClassLevelTestCase.class);

			assertThat(results).hasNumberOfSucceededTests(2);
		}

		@Test
		@DisplayName("throws when configuration is bad")
		void shouldThrowWithBadConfiguration() {
			ExecutionResults results = executeTestClass(BadClassLevelConfigurationTestCase.class);

			assertThat(results).hasNumberOfStartedTests(0);
			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessageContaining("@DefaultTimeZone not configured correctly.");
		}

		@Test
		@DisplayName("does not throw when explicitly set to GMT")
		void shouldNotThrowForExplicitGmt() {
			ExecutionResults results = executeTestClass(ExplicitGmtClassLevelTestCase.class);

			assertThat(results).hasSingleSucceededTest();
		}

		@AfterEach
		void tearDown() {
			assertThat(TimeZone.getDefault()).isEqualTo(TEST_DEFAULT_TIMEZONE);
		}

	}

	@DefaultTimeZone("GMT-8:00")
	static class ClassLevelTestCase {

		@Test
		void shouldExecuteWithClassLevelTimeZone() {
			assertThat(TimeZone.getDefault()).isEqualTo(TimeZone.getTimeZone("GMT-8:00"));
		}

		@Test
		@DefaultTimeZone("GMT-12:00")
		void shouldBeOverriddenWithMethodLevelTimeZone() {
			assertThat(TimeZone.getDefault()).isEqualTo(TimeZone.getTimeZone("GMT-12:00"));
		}

	}

	@DisplayName("with nested classes")
	@DefaultTimeZone("GMT-8:00")
	@Nested
	class NestedDefaultTimeZoneTests {

		@Nested
		@DisplayName("without DefaultTimeZone annotation")
		class NestedClass {

			@Test
			@DisplayName("DefaultTimeZone should be set from enclosed class when it is not provided in nested")
			public void shouldSetTimeZoneFromEnclosedClass() {
				assertThat(TimeZone.getDefault()).isEqualTo(TimeZone.getTimeZone("GMT-8:00"));
			}

		}

		@Nested
		@DefaultTimeZone("GMT-12:00")
		@DisplayName("with DefaultTimeZone annotation")
		class AnnotatedNestedClass {

			@Test
			@DisplayName("DefaultTimeZone should be set from nested class when it is provided")
			public void shouldSetTimeZoneFromNestedClass() {
				assertThat(TimeZone.getDefault()).isEqualTo(TimeZone.getTimeZone("GMT-12:00"));
			}

			@Test
			@DefaultTimeZone("GMT-6:00")
			@DisplayName("DefaultTimeZone should be set from method when it is provided")
			public void shouldSetTimeZoneFromMethodOfNestedClass() {
				assertThat(TimeZone.getDefault()).isEqualTo(TimeZone.getTimeZone("GMT-6:00"));
			}

		}

	}

	static class BadMethodLevelConfigurationTestCase {

		@DefaultTimeZone("Gibberish")
		@Test
		void badConfiguration() {
			fail("This test should never execute");
		}

	}

	@DefaultTimeZone("Gibberish")
	static class BadClassLevelConfigurationTestCase {

		@Test
		void badConfiguration() {
			fail("This test should never execute");
		}

	}

	@DefaultTimeZone("GMT")
	static class ExplicitGmtClassLevelTestCase {

		@Test
		void explicitGmt() {
			assertThat(TimeZone.getDefault()).isEqualTo(TimeZone.getTimeZone("GMT"));
		}

	}

}
