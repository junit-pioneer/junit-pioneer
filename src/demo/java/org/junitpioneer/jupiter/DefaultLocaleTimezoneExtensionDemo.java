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

import java.util.Locale;
import java.util.TimeZone;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class DefaultLocaleTimezoneExtensionDemo {

	// tag::default_locale_language[]
	@Test
	@DefaultLocale("zh-Hant-TW")
	void test_with_language() {
		assertThat(Locale.getDefault()).isEqualTo(Locale.forLanguageTag("zh-Hant-TW"));
	}
	// end::default_locale_language[]

	// tag::default_locale_language_alternatives[]
	@Test
	@DefaultLocale(language = "en")
	void test_with_language_only() {
		assertThat(Locale.getDefault()).isEqualTo(new Locale.Builder().setLanguage("en").build());
	}

	@Test
	@DefaultLocale(language = "en", country = "EN")
	void test_with_language_and_country() {
		assertThat(Locale.getDefault()).isEqualTo(new Locale.Builder().setLanguage("en").setRegion("EN").build());
	}

	@Test
	@DefaultLocale(language = "ja", country = "JP", variant = "japanese")
	void test_with_language_and_country_and_vairant() {
		assertThat(Locale.getDefault())
				.isEqualTo(new Locale.Builder().setLanguage("ja").setRegion("JP").setVariant("japanese").build());
	}
	// end::default_locale_language_alternatives[]

	@Nested
	// tag::default_locale_class_level[]
	@DefaultLocale(language = "fr")
	class MyLocaleTests {

		@Test
		void test_with_class_level_configuration() {
			assertThat(Locale.getDefault()).isEqualTo(new Locale.Builder().setLanguage("fr").build());
		}

		@Test
		@DefaultLocale(language = "en")
		void test_with_method_level_configuration() {
			assertThat(Locale.getDefault()).isEqualTo(new Locale.Builder().setLanguage("en").build());
		}

	}
	// end::default_locale_class_level[]

	// tag::default_timezone_zone[]
	@Test
	@DefaultTimeZone("CET")
	void test_with_short_zone_id() {
		assertThat(TimeZone.getDefault()).isEqualTo(TimeZone.getTimeZone("CET"));
	}

	@Test
	@DefaultTimeZone("Africa/Juba")
	void test_with_long_zone_id() {
		assertThat(TimeZone.getDefault()).isEqualTo(TimeZone.getTimeZone("Africa/Juba"));
	}
	// end::default_timezone_zone[]

	@Nested
	// tag::default_timezone_class_level[]
	@DefaultTimeZone("CET")
	class MyTimeZoneTests {

		@Test
		void test_with_class_level_configuration() {
			assertThat(TimeZone.getDefault()).isEqualTo(TimeZone.getTimeZone("CET"));
		}

		@Test
		@DefaultTimeZone("Africa/Juba")
		void test_with_method_level_configuration() {
			assertThat(TimeZone.getDefault()).isEqualTo(TimeZone.getTimeZone("Africa/Juba"));
		}

	}
	// end::default_timezone_class_level[]

}
