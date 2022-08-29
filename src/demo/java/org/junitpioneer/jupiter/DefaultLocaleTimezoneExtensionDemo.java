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

import java.util.Locale;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;
import org.junitpioneer.internal.PioneerUtils;

public class DefaultLocaleTimezoneExtensionDemo {

	// tag::default_locale_language[]
	@Test
	@DefaultLocale("zh-Hant-TW")
	void test_with_lanuage() {
		assertThat(Locale.getDefault()).isEqualTo(Locale.forLanguageTag("zh-Hant-TW"));
	}
	// end::default_locale_language[]

	// tag::default_locale_language_alternatives[]
	@Test
	@DefaultLocale(language = "en")
	void test_with_lanuage_only() {
		assertThat(Locale.getDefault()).isEqualTo(PioneerUtils.createLocale("en"));
	}

	@Test
	@DefaultLocale(language = "en", country = "EN")
	void test_with_lanuage_and_country() {
		assertThat(Locale.getDefault()).isEqualTo(PioneerUtils.createLocale("en", "EN"));
	}

	@Test
	@DefaultLocale(language = "ja", country = "JP", variant = "japanese")
	void test_with_lanuage_and_country_and_vairant() {
		assertThat(Locale.getDefault()).isEqualTo(PioneerUtils.createLocale("ja", "JP", "japanese"));
	}
	// end::default_locale_language_alternatives[]

	// tag::default_locale_class_level[]
	@DefaultLocale(language = "fr")
	class MyLocaleTests {

		@Test
		void test_with_class_level_configuration() {
			assertThat(Locale.getDefault()).isEqualTo(PioneerUtils.createLocale("fr"));
		}

		@Test
		@DefaultLocale(language = "en")
		void test_with_method_level_configuration() {
			assertThat(Locale.getDefault()).isEqualTo(PioneerUtils.createLocale("en"));
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
