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

import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junitpioneer.AbstractPioneerTestEngineTests;

@DisplayName("DefaultLocale extension with nested classes")
@DefaultLocale(language = "en")
public class NestedDefaultLocaleTests extends AbstractPioneerTestEngineTests {

	@Nested
	@DisplayName("Nested class without DefaultLocale annotation")
	class NestedClass {

		@Test
		@DisplayName("DefaultLocale should be set from enclosed class when it is not provided in nested")
		public void shouldSetLocaleFromEnclosedClass() {
			assertEquals(Locale.getDefault().getLanguage(), "en");
		}
	}

	@Nested
	@DefaultLocale(language = "de")
	@DisplayName("Nested class with DefaultLocale annotation")
	class AnnotatedNestedClass {

		@Test
		@DisplayName("DefaultLocale should be set from nested class when it is provided")
		public void shouldSetLocaleFromNestedClass() {
			assertEquals(Locale.getDefault().getLanguage(), "de");
		}

		@Test
		@DefaultLocale(language = "ch")
		@DisplayName("DefaultLocale should be set from method when it is provided")
		public void shouldSetLocaleFromMethodOfNestedClass() {
			assertEquals(Locale.getDefault().getLanguage(), "ch");
		}
	}

}
