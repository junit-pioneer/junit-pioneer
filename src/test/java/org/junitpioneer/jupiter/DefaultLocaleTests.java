/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junitpioneer.AbstractPioneerTestEngineTests;

@DisplayName("DefaultLocale extension")
class DefaultLocaleTests extends AbstractPioneerTestEngineTests {

	private static Locale TEST_DEFAULT_LOCALE;
	private static Locale DEFAULT_LOCALE_BEFORE_TEST;

	@BeforeAll
	static void globalSetUp() {
		DEFAULT_LOCALE_BEFORE_TEST = Locale.getDefault();
		TEST_DEFAULT_LOCALE = new Locale("custom");
		Locale.setDefault(TEST_DEFAULT_LOCALE);
	}

	@AfterAll
	static void globalTearDown() {
		Locale.setDefault(DEFAULT_LOCALE_BEFORE_TEST);
	}

	@Nested
	@DisplayName("applied on the method level")
	class MethodLevelTests {

		@Test
		@DisplayName("does nothing when annotation is not present")
		void testDefaultLocaleNoAnnotation() {
			assertEquals(TEST_DEFAULT_LOCALE, Locale.getDefault());
		}

		@DefaultLocale(language = "en_EN")
		@Test
		@DisplayName("sets the default locale using a language")
		void setsLanguage() {
			assertEquals(new Locale("en_EN"), Locale.getDefault());
		}

		@DefaultLocale(language = "en", country = "EN")
		@Test
		@DisplayName("sets the default locale using a language and a country")
		void setsLanguageAndCountry() {
			assertEquals(new Locale("en", "EN"), Locale.getDefault());
		}

		@DefaultLocale(language = "en", country = "EN", variant = "gb")
		@Test
		@DisplayName("sets the default locale using a language, a country and a variant")
		void setsLanguageAndCountryAndVariant() {
			assertEquals(new Locale("en", "EN", "gb"), Locale.getDefault());
		}
	}

	@Nested
	@DisplayName("applied on the class level")
	class ClassLevelTests {

		@BeforeEach
		void setUp() {
			assertEquals(TEST_DEFAULT_LOCALE, Locale.getDefault());
		}

		@Test
		@DisplayName("should execute tests with configured Locale")
		void shouldExecuteTestsWithConfiguredLocale() {
			ExecutionEventRecorder eventRecorder = executeTestsForClass(ClassLevelTestCase.class);

			assertEquals(2, eventRecorder.getTestSuccessfulCount());
		}

		@AfterEach
		void tearDown() {
			assertEquals(TEST_DEFAULT_LOCALE, Locale.getDefault());
		}
	}

	@DefaultLocale(language = "fr", country = "FR")
	static class ClassLevelTestCase {

		@Test
		void shouldExecuteWithClassLevelLocale() {
			assertEquals(new Locale("fr", "FR"), Locale.getDefault());
		}

		@Test
		@DefaultLocale(language = "de", country = "DE")
		void shouldBeOverriddenWithMethodLevelLocale() {
			assertEquals(new Locale("de", "DE"), Locale.getDefault());
		}
	}

	@Nested
	@DisplayName("when configured incorrect")
	class ConfigurationFailureTests {

		@Test
		@DisplayName("should fail when variant is set but country is not on method level")
		void shouldFailWhenVariantIsSetButCountryIsNotOnMethodLevel() {
			ExecutionEventRecorder eventRecorder = executeTestsForClass(MethodLevelInitializationFailureTestCase.class);

			assertEquals(1, eventRecorder.getTestFailedCount());
			//@formatter:off
			Throwable thrown = eventRecorder.getFailedTestFinishedEvents().get(0)
					.getPayload(TestExecutionResult.class)
					.flatMap(TestExecutionResult::getThrowable)
					.orElseThrow(AssertionError::new);
			//@formatter:on
			assertTrue(thrown instanceof ExtensionConfigurationException);
		}

		@Test
		@DisplayName("should fail when variant is set but country is not on class level")
		void shouldFailWhenVariantIsSetButCountryIsNotOnClassLevel() {
			ExecutionEventRecorder eventRecorder = executeTestsForClass(ClassLevelInitializationFailureTestCase.class);

			assertEquals(1, eventRecorder.getContainerFailedCount());
			//@formatter:off
			Throwable thrown = eventRecorder.getFailedContainerEvents().get(0)
					.getPayload(TestExecutionResult.class)
					.flatMap(TestExecutionResult::getThrowable)
					.orElseThrow(AssertionError::new);
			//@formatter:on
			assertTrue(thrown instanceof ExtensionConfigurationException);
		}
	}

	static class MethodLevelInitializationFailureTestCase {

		@Test
		@DefaultLocale(language = "de", variant = "ch")
		void shouldFail() {
		}
	}

	@DefaultLocale(language = "de", variant = "ch")
	static class ClassLevelInitializationFailureTestCase {

		@Test
		void shouldFail() {
		}
	}

}
