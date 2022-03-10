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
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

@DisplayName("SystemProperty extension")
class SystemPropertyExtensionTests {

	@BeforeAll
	static void globalSetUp() {
		System.setProperty("set prop A", "old A");
		System.setProperty("set prop B", "old B");
		System.setProperty("set prop C", "old C");

		System.clearProperty("clear prop D");
		System.clearProperty("clear prop E");
		System.clearProperty("clear prop F");
	}

	@AfterAll
	static void globalTearDown() {
		assertThat(System.getProperty("set prop A")).isEqualTo("old A");
		System.clearProperty("set prop A");
		assertThat(System.getProperty("set prop B")).isEqualTo("old B");
		System.clearProperty("set prop B");
		assertThat(System.getProperty("set prop C")).isEqualTo("old C");
		System.clearProperty("set prop C");

		assertThat(System.getProperty("clear prop D")).isNull();
		assertThat(System.getProperty("clear prop E")).isNull();
		assertThat(System.getProperty("clear prop F")).isNull();
	}

	@Nested
	@DisplayName("used with ClearSystemProperty")
	@ClearSystemProperty(key = "set prop A")
	class ClearSystemPropertyTests {

		@Test
		@DisplayName("should clear system property")
		@ClearSystemProperty(key = "set prop B")
		void shouldClearSystemProperty() {
			assertThat(System.getProperty("set prop A")).isNull();
			assertThat(System.getProperty("set prop B")).isNull();
			assertThat(System.getProperty("set prop C")).isEqualTo("old C");

			assertThat(System.getProperty("clear prop D")).isNull();
			assertThat(System.getProperty("clear prop E")).isNull();
			assertThat(System.getProperty("clear prop F")).isNull();
		}

		@Test
		@DisplayName("should be repeatable")
		@ClearSystemProperty(key = "set prop B")
		@ClearSystemProperty(key = "set prop C")
		void shouldBeRepeatable() {
			assertThat(System.getProperty("set prop A")).isNull();
			assertThat(System.getProperty("set prop B")).isNull();
			assertThat(System.getProperty("set prop C")).isNull();

			assertThat(System.getProperty("clear prop D")).isNull();
			assertThat(System.getProperty("clear prop E")).isNull();
			assertThat(System.getProperty("clear prop F")).isNull();
		}

	}

	@Nested
	@DisplayName("used with SetSystemProperty")
	@SetSystemProperty(key = "set prop A", value = "new A")
	class SetSystemPropertyTests {

		@Test
		@DisplayName("should set system property to value")
		@SetSystemProperty(key = "set prop B", value = "new B")
		void shouldSetSystemPropertyToValue() {
			assertThat(System.getProperty("set prop A")).isEqualTo("new A");
			assertThat(System.getProperty("set prop B")).isEqualTo("new B");
			assertThat(System.getProperty("set prop C")).isEqualTo("old C");

			assertThat(System.getProperty("clear prop D")).isNull();
			assertThat(System.getProperty("clear prop E")).isNull();
			assertThat(System.getProperty("clear prop F")).isNull();
		}

		@Test
		@DisplayName("should be repeatable")
		@SetSystemProperty(key = "set prop B", value = "new B")
		@SetSystemProperty(key = "clear prop D", value = "new D")
		void shouldBeRepeatable() {
			assertThat(System.getProperty("set prop A")).isEqualTo("new A");
			assertThat(System.getProperty("set prop B")).isEqualTo("new B");
			assertThat(System.getProperty("set prop C")).isEqualTo("old C");

			assertThat(System.getProperty("clear prop D")).isEqualTo("new D");
			assertThat(System.getProperty("clear prop E")).isNull();
			assertThat(System.getProperty("clear prop F")).isNull();
		}

	}

	@Nested
	@DisplayName("used with both ClearSystemProperty and SetSystemProperty")
	@ClearSystemProperty(key = "set prop A")
	@SetSystemProperty(key = "clear prop D", value = "new D")
	class CombinedSystemPropertyTests {

		@Test
		@DisplayName("should be combinable")
		@ClearSystemProperty(key = "set prop B")
		@SetSystemProperty(key = "clear prop E", value = "new E")
		void clearAndSetSystemPropertyShouldBeCombinable() {
			assertThat(System.getProperty("set prop A")).isNull();
			assertThat(System.getProperty("set prop B")).isNull();
			assertThat(System.getProperty("set prop C")).isEqualTo("old C");

			assertThat(System.getProperty("clear prop D")).isEqualTo("new D");
			assertThat(System.getProperty("clear prop E")).isEqualTo("new E");
			assertThat(System.getProperty("clear prop F")).isNull();
		}

		@Test
		@DisplayName("method level should overwrite class level")
		@ClearSystemProperty(key = "clear prop D")
		@SetSystemProperty(key = "set prop A", value = "new A")
		void methodLevelShouldOverwriteClassLevel() {
			assertThat(System.getProperty("set prop A")).isEqualTo("new A");
			assertThat(System.getProperty("set prop B")).isEqualTo("old B");
			assertThat(System.getProperty("set prop C")).isEqualTo("old C");

			assertThat(System.getProperty("clear prop D")).isNull();
			assertThat(System.getProperty("clear prop E")).isNull();
			assertThat(System.getProperty("clear prop F")).isNull();
		}

		@Test
		@Issue("473")
		@DisplayName("method level should not clash (in terms of duplicate entries) with class level")
		@SetSystemProperty(key = "set prop A", value = "new A")
		void methodLevelShouldNotClashWithClassLevel() {
			assertThat(System.getProperty("set prop A")).isEqualTo("new A");
			assertThat(System.getProperty("set prop B")).isEqualTo("old B");
			assertThat(System.getProperty("set prop C")).isEqualTo("old C");
			assertThat(System.getProperty("clear prop D")).isEqualTo("new D");

			assertThat(System.getProperty("clear prop E")).isNull();
			assertThat(System.getProperty("clear prop F")).isNull();
		}

	}

	@DisplayName("with nested classes")
	@ClearSystemProperty(key = "set prop A")
	@SetSystemProperty(key = "set prop B", value = "new B")
	@Nested
	class NestedSystemPropertyTests {

		@Nested
		@TestMethodOrder(OrderAnnotation.class)
		@DisplayName("without SystemProperty annotations")
		class NestedClass {

			@Test
			@Order(1)
			@ReadsSystemProperty
			@DisplayName("system properties should be set from enclosed class when they are not provided in nested")
			void shouldSetSystemPropertyFromEnclosedClass() {
				assertThat(System.getProperty("set prop A")).isNull();
				assertThat(System.getProperty("set prop B")).isEqualTo("new B");
			}

			@Test
			@Issue("480")
			@Order(2)
			@ReadsSystemProperty
			@DisplayName("system properties should be set from enclosed class after restore")
			void shouldSetSystemPropertyFromEnclosedClassAfterRestore() {
				assertThat(System.getProperty("set prop A")).isNull();
				assertThat(System.getProperty("set prop B")).isEqualTo("new B");
			}

		}

		@Nested
		@SetSystemProperty(key = "set prop B", value = "newer B")
		@DisplayName("with SetSystemProperty annotation")
		class AnnotatedNestedClass {

			@Test
			@ReadsSystemProperty
			@DisplayName("system property should be set from nested class when it is provided")
			void shouldSetSystemPropertyFromNestedClass() {
				assertThat(System.getProperty("set prop B")).isEqualTo("newer B");
			}

			@Test
			@SetSystemProperty(key = "set prop B", value = "newest B")
			@DisplayName("system property should be set from method when it is provided")
			void shouldSetSystemPropertyFromMethodOfNestedClass() {
				assertThat(System.getProperty("set prop B")).isEqualTo("newest B");
			}

		}

	}

	@Nested
	@DisplayName("used with incorrect configuration")
	class ConfigurationFailureTests {

		@Test
		@DisplayName("should fail when clear and set same system property")
		void shouldFailWhenClearAndSetSameSystemProperty() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethod(MethodLevelInitializationFailureTestCases.class,
						"shouldFailWhenClearAndSetSameSystemProperty");

			assertThat(results).hasSingleFailedTest().withExceptionInstanceOf(ExtensionConfigurationException.class);
		}

		@Test
		@DisplayName("should fail when clear same system property twice")
		@Disabled("This can't happen at the moment, because Jupiter's annotation tooling "
				+ "deduplicates identical annotations like the ones required for this test: "
				+ "https://github.com/junit-team/junit5/issues/2131")
		void shouldFailWhenClearSameSystemPropertyTwice() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethod(MethodLevelInitializationFailureTestCases.class,
						"shouldFailWhenClearSameSystemPropertyTwice");

			assertThat(results).hasSingleFailedTest().withExceptionInstanceOf(ExtensionConfigurationException.class);
		}

		@Test
		@DisplayName("should fail when set same system property twice")
		void shouldFailWhenSetSameSystemPropertyTwice() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethod(MethodLevelInitializationFailureTestCases.class,
						"shouldFailWhenSetSameSystemPropertyTwice");
			assertThat(results).hasSingleFailedTest().withExceptionInstanceOf(ExtensionConfigurationException.class);
		}

	}

	static class MethodLevelInitializationFailureTestCases {

		@Test
		@DisplayName("clearing and setting the same property")
		@ClearSystemProperty(key = "set prop A")
		@SetSystemProperty(key = "set prop A", value = "new A")
		void shouldFailWhenClearAndSetSameSystemProperty() {
		}

		@Test
		@ClearSystemProperty(key = "set prop A")
		@ClearSystemProperty(key = "set prop A")
		void shouldFailWhenClearSameSystemPropertyTwice() {
		}

		@Test
		@SetSystemProperty(key = "set prop A", value = "new A")
		@SetSystemProperty(key = "set prop A", value = "new B")
		void shouldFailWhenSetSameSystemPropertyTwice() {
		}

	}

	@Nested
	@DisplayName("used with inheritance")
	class InheritanceTests extends InheritanceBaseTest {

		@Test
		@Issue("448")
		@DisplayName("should inherit clear and set annotations")
		void shouldInheritClearAndSetProperty() {
			assertThat(System.getProperty("set prop A")).isNull();
			assertThat(System.getProperty("set prop B")).isNull();
			assertThat(System.getProperty("clear prop D")).isEqualTo("new D");
			assertThat(System.getProperty("clear prop E")).isEqualTo("new E");
		}

	}

	@ClearSystemProperty(key = "set prop A")
	@ClearSystemProperty(key = "set prop B")
	@SetSystemProperty(key = "clear prop D", value = "new D")
	@SetSystemProperty(key = "clear prop E", value = "new E")
	static class InheritanceBaseTest {

	}

}
