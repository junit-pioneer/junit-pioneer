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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("SystemProperty extension")
class SystemPropertyExtensionTests {

	@BeforeAll
	static void globalSetUp() {
		System.setProperty("property A", "old A");
		System.setProperty("property B", "old B");
	}

	@AfterAll
	static void globalTearDown() {
		assertThat(System.getProperty("property A")).isEqualTo("old A");
		assertThat(System.getProperty("property B")).isEqualTo("old B");
	}

	@Nested
	@DisplayName("used with ClearSystemProperty")
	class ClearSystemPropertyTests {

		@Test
		@DisplayName("should clear system property")
		@ClearSystemProperty(key = "property A")
		void shouldClearSystemProperty() {
			assertThat(System.getProperty("property A")).isNull();
			assertThat(System.getProperty("property B")).isEqualTo("old B");
		}

		@Test
		@DisplayName("should be repeatable")
		@ClearSystemProperty(key = "property A")
		@ClearSystemProperty(key = "property B")
		void shouldBeRepeatable() {
			assertThat(System.getProperty("property A")).isNull();
			assertThat(System.getProperty("property B")).isNull();
		}

	}

	@Nested
	@DisplayName("used with SetSystemProperty")
	class SetSystemPropertyTests {

		@Test
		@DisplayName("should set system property to value")
		@SetSystemProperty(key = "property A", value = "new A")
		void shouldSetSystemPropertyToValue() {
			assertThat(System.getProperty("property A")).isEqualTo("new A");
			assertThat(System.getProperty("property B")).isEqualTo("old B");
		}

		@Test
		@DisplayName("should be repeatable")
		@SetSystemProperty(key = "property A", value = "new A")
		@SetSystemProperty(key = "property B", value = "new B")
		void shouldBeRepeatable() {
			assertThat(System.getProperty("property A")).isEqualTo("new A");
			assertThat(System.getProperty("property B")).isEqualTo("new B");
		}

	}

	@Nested
	@DisplayName("used with both ClearSystemProperty and SetSystemProperty")
	class MixedSystemPropertyTests {

		@Test
		@DisplayName("should be mixable")
		@ClearSystemProperty(key = "property A")
		@SetSystemProperty(key = "property B", value = "new B")
		void clearAndSetSystemPropertyShouldBeMixable() {
			assertThat(System.getProperty("property A")).isNull();
			assertThat(System.getProperty("property B")).isEqualTo("new B");
		}

	}

}
