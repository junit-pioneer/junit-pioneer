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

import java.util.Locale;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DefaultLocale extension")
class DefaultLocaleTests {

	private static Locale TEST_DEFAULT_LOCALE;
	private static Locale DEFAULT_LOCALE_BEFORE_TEST;

	@BeforeAll
	static void globalSetUp() {
		DEFAULT_LOCALE_BEFORE_TEST = Locale.getDefault();
		if (!DEFAULT_LOCALE_BEFORE_TEST.equals(Locale.CANADA)) {
			Locale.setDefault(Locale.CANADA);
		}
		else {
			// you seem to be from Canada...
			Locale.setDefault(Locale.CHINESE);
		}
		TEST_DEFAULT_LOCALE = Locale.getDefault();
	}

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

	@Disabled("TODO: How to test this?")
	@DefaultLocale(language = "en", variant = "gb")
	@Test
	@DisplayName("fails when variant is set but country is not")
	void failsWhenVariantIsSetButCountryIsNotSet() throws Exception {
		assertEquals(new Locale("en_EN"), Locale.getDefault());
	}

	@AfterAll
	static void globalTearDown() {
		Locale.setDefault(DEFAULT_LOCALE_BEFORE_TEST);
	}
}
