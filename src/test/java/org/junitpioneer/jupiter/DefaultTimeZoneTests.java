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
import static org.junitpioneer.testkit.PioneerTestKit.executeTestClass;
import static org.junitpioneer.testkit.PioneerTestKit.executeTestMethod;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.util.TimeZone;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
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
		// we set UTC as test time zone unless it is already
		// the system's time zone; in that case we use UTC+12
		DEFAULT_TIMEZONE_BEFORE_TEST = TimeZone.getDefault();
		TimeZone utc = TimeZone.getTimeZone("UTC");
		TimeZone utcPlusTwelve = TimeZone.getTimeZone("GMT+12:00");
		if (DEFAULT_TIMEZONE_BEFORE_TEST.equals(utc))
			TimeZone.setDefault(utcPlusTwelve);
		else
			TimeZone.setDefault(utc);
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
		@ReadsDefaultTimeZone
		@DisplayName("does nothing when annotation is not present")
		void doesNothingWhenAnnotationNotPresent() {
			assertThat(TimeZone.getDefault()).isEqualTo(TEST_DEFAULT_TIMEZONE);
		}

		@Test
		@DefaultTimeZone("GMT")
		@DisplayName("does not throw when explicitly set to GMT")
		void doesNotThrowForExplicitGmt() {
			assertThat(TimeZone.getDefault()).isEqualTo(TimeZone.getTimeZone("GMT"));
		}

		@Test
		@DefaultTimeZone("CET")
		@DisplayName("sets the default time zone using an abbreviation")
		void setsTimeZoneFromAbbreviation() {
			assertThat(TimeZone.getDefault()).isEqualTo(TimeZone.getTimeZone("CET"));
		}

		@Test
		@DefaultTimeZone("America/Los_Angeles")
		@DisplayName("sets the default time zone using a full name")
		void setsTimeZoneFromFullName() {
			assertThat(TimeZone.getDefault()).isEqualTo(TimeZone.getTimeZone("America/Los_Angeles"));
		}

	}

	@Nested
	@DefaultTimeZone("GMT-8:00")
	@DisplayName("when applied on the class level")
	class ClassLevelTestCase {

		@Test
		@ReadsDefaultTimeZone
		@DisplayName("sets the default time zone")
		void shouldExecuteWithClassLevelTimeZone() {
			assertThat(TimeZone.getDefault()).isEqualTo(TimeZone.getTimeZone("GMT-8:00"));
		}

		@Test
		@DefaultTimeZone("GMT-12:00")
		@DisplayName("gets overridden by annotation on the method level")
		void shouldBeOverriddenWithMethodLevelTimeZone() {
			assertThat(TimeZone.getDefault()).isEqualTo(TimeZone.getTimeZone("GMT-12:00"));
		}

	}

	@Nested
	@DefaultTimeZone("GMT")
	@DisplayName("when explicitly set to GMT on the class level")
	class ExplicitGmtClassLevelTestCase {

		@Test
		@DisplayName("does not throw and sets to GMT ")
		void explicitGmt() {
			assertThat(TimeZone.getDefault()).isEqualTo(TimeZone.getTimeZone("GMT"));
		}

	}

	@Nested
	@DefaultTimeZone("GMT-8:00")
	@DisplayName("with nested classes")
	class NestedTests {

		@Nested
		@DisplayName("without DefaultTimeZone annotation")
		class NestedClass {

			@Test
			@ReadsDefaultTimeZone
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
			@ReadsDefaultTimeZone
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

	@Nested
	@DisplayName("when misconfigured")
	class ConfigurationTests {

		@Test
		@ReadsDefaultTimeZone
		@DisplayName("on method level, throws exception")
		void throwsWhenConfigurationIsBad() {
			ExecutionResults results = executeTestMethod(BadMethodLevelConfigurationTestCase.class, "badConfiguration");

			assertThat(results)
					.hasSingleFailedTest()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessageNotContaining("should never execute")
					.hasMessageContaining("@DefaultTimeZone not configured correctly.");
		}

		@Test
		@ReadsDefaultTimeZone
		@DisplayName("on class level, throws exception")
		void shouldThrowWithBadConfiguration() {
			ExecutionResults results = executeTestClass(BadClassLevelConfigurationTestCase.class);

			assertThat(results)
					.hasSingleFailedTest()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessageContaining("@DefaultTimeZone not configured correctly.");
		}

		@AfterEach
		void verifyMisconfigurationSisNotChangeTimeZone() {
			assertThat(TimeZone.getDefault()).isEqualTo(TEST_DEFAULT_TIMEZONE);
		}

	}

	static class BadMethodLevelConfigurationTestCase {

		@Test
		@DefaultTimeZone("Gibberish")
		void badConfiguration() {
		}

	}

	@DefaultTimeZone("Gibberish")
	static class BadClassLevelConfigurationTestCase {

		@Test
		void badConfiguration() {
		}

	}

}
