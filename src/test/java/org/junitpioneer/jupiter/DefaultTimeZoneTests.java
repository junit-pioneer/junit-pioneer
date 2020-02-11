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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.TimeZone;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junitpioneer.AbstractPioneerTestEngineTests;

@DisplayName("DefaultTimeZone extension")
class DefaultTimeZoneTests extends AbstractPioneerTestEngineTests {

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
			assertEquals(TEST_DEFAULT_TIMEZONE, TimeZone.getDefault());
		}

		@DefaultTimeZone("CET")
		@Test
		@DisplayName("sets the default time zone using an abbreviation")
		void setsTimeZoneFromAbbreviation() {
			assertEquals(TimeZone.getTimeZone("CET"), TimeZone.getDefault());
		}

		@DefaultTimeZone("America/Los_Angeles")
		@Test
		@DisplayName("sets the default time zone using a full name")
		void setsTimeZoneFromFullName() {
			assertEquals(TimeZone.getTimeZone("America/Los_Angeles"), TimeZone.getDefault());
		}

	}

	@Nested
	@DisplayName("applied on the class level")
	class ClassLevelTests {

		@BeforeEach
		void setUp() {
			assertEquals(TEST_DEFAULT_TIMEZONE, TimeZone.getDefault());
		}

		@Test
		@DisplayName("should execute tests with configured TimeZone")
		void shouldExecuteTestsWithConfiguredTimeZone() {
			ExecutionEventRecorder eventRecorder = executeTestsForClass(DefaultTimeZoneTests.ClassLevelTestCase.class);

			assertEquals(2, eventRecorder.getTestSuccessfulCount());
		}

		@AfterEach
		void tearDown() {
			assertEquals(TEST_DEFAULT_TIMEZONE, TimeZone.getDefault());
		}

	}

	@DefaultTimeZone("GMT-8:00")
	static class ClassLevelTestCase {

		@Test
		void shouldExecuteWithClassLevelTimeZone() {
			assertEquals(TimeZone.getTimeZone("GMT-8:00"), TimeZone.getDefault());
		}

		@Test
		@DefaultTimeZone("GMT-12:00")
		void shouldBeOverriddenWithMethodLevelTimeZone() {
			assertEquals(TimeZone.getTimeZone("GMT-12:00"), TimeZone.getDefault());
		}

	}

	@DisplayName("with nested classes")
	@DefaultTimeZone("GMT-8:00")
	@Nested
	class NestedDefaultTimeZoneTests extends AbstractPioneerTestEngineTests {

		@Nested
		@DisplayName("without DefaultTimeZone annotation")
		class NestedClass {

			@Test
			@DisplayName("DefaultTimeZone should be set from enclosed class when it is not provided in nested")
			public void shouldSetTimeZoneFromEnclosedClass() {
				assertEquals(TimeZone.getTimeZone("GMT-8:00"), TimeZone.getDefault());
			}

		}

		@Nested
		@DefaultTimeZone("GMT-12:00")
		@DisplayName("with DefaultTimeZone annotation")
		class AnnotatedNestedClass {

			@Test
			@DisplayName("DefaultTimeZone should be set from nested class when it is provided")
			public void shouldSetTimeZoneFromNestedClass() {
				assertEquals(TimeZone.getTimeZone("GMT-12:00"), TimeZone.getDefault());
			}

			@Test
			@DefaultTimeZone("GMT-6:00")
			@DisplayName("DefaultTimeZone should be set from method when it is provided")
			public void shouldSetTimeZoneFromMethodOfNestedClass() {
				assertEquals(TimeZone.getTimeZone("GMT-6:00"), TimeZone.getDefault());
			}

		}

	}

}
