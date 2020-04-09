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

import java.util.List;
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
import org.junit.platform.engine.test.event.ExecutionEvent;
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
			assertThat(Locale.getDefault()).isEqualTo(TEST_DEFAULT_LOCALE);
		}

		@DefaultLocale("zh-Hant-TW")
		@Test
		@DisplayName("sets the default locale using a language tag")
		void setsLocaleViaLanguageTag() {
			assertThat(Locale.getDefault()).isEqualTo(Locale.forLanguageTag("zh-Hant-TW"));
		}

		@DefaultLocale(language = "en_EN")
		@Test
		@DisplayName("sets the default locale using a language")
		void setsLanguage() {
			assertThat(Locale.getDefault()).isEqualTo(new Locale("en_EN"));
		}

		@DefaultLocale(language = "en", country = "EN")
		@Test
		@DisplayName("sets the default locale using a language and a country")
		void setsLanguageAndCountry() {
			assertThat(Locale.getDefault()).isEqualTo(new Locale("en", "EN"));
		}

		@DefaultLocale(language = "en", country = "EN", variant = "gb")
		@Test
		@DisplayName("sets the default locale using a language, a country and a variant")
		void setsLanguageAndCountryAndVariant() {
			assertThat(Locale.getDefault()).isEqualTo(new Locale("en", "EN", "gb"));
		}

	}

	@Nested
	@DisplayName("applied on the class level")
	class ClassLevelTests {

		@BeforeEach
		void setUp() {
			assertThat(Locale.getDefault()).isEqualTo(TEST_DEFAULT_LOCALE);
		}

		@Test
		@DisplayName("should execute tests with configured Locale")
		void shouldExecuteTestsWithConfiguredLocale() {
			ExecutionEventRecorder eventRecorder = executeTestsForClass(ClassLevelTestCase.class);
			assertThat(eventRecorder.getTestSuccessfulCount()).isEqualTo(2);
		}

		@AfterEach
		void tearDown() {
			assertThat(Locale.getDefault()).isEqualTo(TEST_DEFAULT_LOCALE);
		}

	}

	@DefaultLocale(language = "fr", country = "FR")
	static class ClassLevelTestCase {

		@Test
		void shouldExecuteWithClassLevelLocale() {
			assertThat(Locale.getDefault()).isEqualTo(new Locale("fr", "FR"));
		}

		@Test
		@DefaultLocale(language = "de", country = "DE")
		void shouldBeOverriddenWithMethodLevelLocale() {
			assertThat(Locale.getDefault()).isEqualTo(new Locale("de", "DE"));
		}

	}

	@DisplayName("with nested classes")
	@DefaultLocale(language = "en")
	@Nested
	class NestedDefaultLocaleTests extends AbstractPioneerTestEngineTests {

		@Nested
		@DisplayName("without DefaultLocale annotation")
		class NestedClass {

			@Test
			@DisplayName("DefaultLocale should be set from enclosed class when it is not provided in nested")
			public void shouldSetLocaleFromEnclosedClass() {
				assertThat("en").isEqualTo(Locale.getDefault().getLanguage());
			}

		}

		@Nested
		@DefaultLocale(language = "de")
		@DisplayName("with DefaultLocale annotation")
		class AnnotatedNestedClass {

			@Test
			@DisplayName("DefaultLocale should be set from nested class when it is provided")
			public void shouldSetLocaleFromNestedClass() {
				assertThat("de").isEqualTo(Locale.getDefault().getLanguage());
			}

			@Test
			@DefaultLocale(language = "ch")
			@DisplayName("DefaultLocale should be set from method when it is provided")
			public void shouldSetLocaleFromMethodOfNestedClass() {
				assertThat("ch").isEqualTo(Locale.getDefault().getLanguage());
			}

		}

	}

	@Nested
	@DisplayName("when configured incorrect")
	class ConfigurationFailureTests {

		@Nested
		@DisplayName("on the method level")
		class MethodLevel {

			@Test
			@DisplayName("should fail when nothing is configured")
			void shouldFailWhenNothingIsConfigured() {
				ExecutionEventRecorder eventRecorder = executeTests(MethodLevelInitializationFailureTestCase.class,
					"shouldFailMissingConfiguration");

				assertExtensionConfigurationFailure(eventRecorder.getFailedTestFinishedEvents());
			}

			@Test
			@DisplayName("should fail when variant is set but country is not")
			void shouldFailWhenVariantIsSetButCountryIsNot() {
				ExecutionEventRecorder eventRecorder = executeTests(MethodLevelInitializationFailureTestCase.class,
					"shouldFailMissingCountry");

				assertExtensionConfigurationFailure(eventRecorder.getFailedTestFinishedEvents());
			}

			@Test
			@DisplayName("should fail when languageTag and language is set")
			void shouldFailWhenLanguageTagAndLanguageIsSet() {
				ExecutionEventRecorder eventRecorder = executeTests(MethodLevelInitializationFailureTestCase.class,
					"shouldFailLanguageTagAndLanguage");

				assertExtensionConfigurationFailure(eventRecorder.getFailedTestFinishedEvents());
			}

			@Test
			@DisplayName("should fail when languageTag and country is set")
			void shouldFailWhenLanguageTagAndCountryIsSet() {
				ExecutionEventRecorder eventRecorder = executeTests(MethodLevelInitializationFailureTestCase.class,
					"shouldFailLanguageTagAndCountry");

				assertExtensionConfigurationFailure(eventRecorder.getFailedTestFinishedEvents());
			}

			@Test
			@DisplayName("should fail when languageTag and variant is set")
			void shouldFailWhenLanguageTagAndVariantIsSet() {
				ExecutionEventRecorder eventRecorder = executeTests(MethodLevelInitializationFailureTestCase.class,
					"shouldFailLanguageTagAndVariant");

				assertExtensionConfigurationFailure(eventRecorder.getFailedTestFinishedEvents());
			}

		}

		@Nested
		@DisplayName("on the class level")
		class ClassLevel {

			@Test
			@DisplayName("should fail when variant is set but country is not")
			void shouldFailWhenVariantIsSetButCountryIsNot() {
				ExecutionEventRecorder eventRecorder = executeTestsForClass(
					ClassLevelInitializationFailureTestCase.class);

				assertExtensionConfigurationFailure(eventRecorder.getFailedContainerEvents());
			}

		}

	}

	static class MethodLevelInitializationFailureTestCase {

		@Test
		@DefaultLocale
		void shouldFailMissingConfiguration() {
		}

		@Test
		@DefaultLocale(language = "de", variant = "ch")
		void shouldFailMissingCountry() {
		}

		@Test
		@DefaultLocale(value = "Something", language = "de")
		void shouldFailLanguageTagAndLanguage() {
		}

		@Test
		@DefaultLocale(value = "Something", country = "DE")
		void shouldFailLanguageTagAndCountry() {
		}

		@Test
		@DefaultLocale(value = "Something", variant = "ch")
		void shouldFailLanguageTagAndVariant() {
		}

	}

	@DefaultLocale(language = "de", variant = "ch")
	static class ClassLevelInitializationFailureTestCase {

		@Test
		void shouldFail() {
		}

	}

	private static void assertExtensionConfigurationFailure(List<ExecutionEvent> failedTestFinishedEvents) {
		assertThat(failedTestFinishedEvents.size()).isEqualTo(1);
		Throwable thrown = failedTestFinishedEvents
				.get(0)
				.getPayload(TestExecutionResult.class)
				.flatMap(TestExecutionResult::getThrowable)
				.orElseThrow(AssertionError::new);
		assertThat(thrown).isInstanceOf(ExtensionConfigurationException.class);
	}

}
