/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.testkit.engine.Event;
import org.junitpioneer.jupiter.ReportEntry;
import org.junitpioneer.jupiter.cartesian.CartesianTest;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

/**
 * Tests for {@link JsonInlineArgumentsProvider}
 */
class JsonSourceArgumentsProviderTests {

	@Test
	void assertAllValuesSupplied() {
		ExecutionResults results = PioneerTestKit.executeTestClass(JsonSourceTests.class);

		Map<String, List<String>> displayNames = results
				.dynamicallyRegisteredEvents()
				.map(Event::getTestDescriptor)
				.collect(Collectors
						.groupingBy(JsonSourceArgumentsProviderTests::testSourceMethodName,
							Collectors.mapping(TestDescriptor::getDisplayName, Collectors.toList())));

		assertThat(displayNames)
				.containsOnlyKeys("deconstructCustomerFromArray", "deconstructCustomerMultipleValues",
					"deconstructCustomerMultipleLinesComplexType", "singleCustomer", "singleCustomerName");

		assertThat(displayNames.get("deconstructCustomerFromArray")).containsExactly("[1] Luke, 172", "[2] Yoda, 66");

		assertThat(displayNames.get("deconstructCustomerMultipleValues"))
				.containsExactly("[1] 66, Yoda", "[2] 172, Luke");

		assertThat(displayNames.get("deconstructCustomerMultipleLinesComplexType"))
				.containsExactly("[1] Yoda, Location{name='unknown'}", "[2] Luke, Location{name='Tatooine'}");

		assertThat(displayNames.get("singleCustomer"))
				.containsExactly("[1] Customer{name='Luke', height=172}", "[2] Customer{name='Yoda', height=66}");

		assertThat(displayNames.get("singleCustomerName")).containsExactly("[1] Luke", "[2] Yoda");
	}

	@Test
	void assertAllCartesianValuesSupplied() {
		ExecutionResults results = PioneerTestKit.executeTestClass(JsonSourceCartesianTests.class);
		Map<String, List<String>> displayNames = results
				.dynamicallyRegisteredEvents()
				.map(Event::getTestDescriptor)
				.collect(Collectors
						.groupingBy(JsonSourceArgumentsProviderTests::testSourceMethodName,
							Collectors.mapping(TestDescriptor::getDisplayName, Collectors.toList())));

		assertThat(displayNames)
				.containsOnlyKeys("extractPropertyFromArray", "extractPropertyFromMultipleValues",
					"extractPropertyMultipleLinesWithComplexType", "constructToParameterTypeWhenNotUsingProperty");

		assertThat(displayNames.get("extractPropertyFromArray"))
				.containsExactly("[1] Luke, 172", "[2] Luke, 66", "[3] Yoda, 172", "[4] Yoda, 66");

		assertThat(displayNames.get("extractPropertyFromMultipleValues"))
				.containsExactly("[1] 66, Yoda", "[2] 66, Luke", "[3] 172, Yoda", "[4] 172, Luke");

		assertThat(displayNames.get("extractPropertyMultipleLinesWithComplexType"))
				.containsExactly("[1] Yoda, Location{name='unknown'}", "[2] Yoda, Location{name='Tatooine'}",
					"[3] Luke, Location{name='unknown'}", "[4] Luke, Location{name='Tatooine'}");

		assertThat(displayNames.get("constructToParameterTypeWhenNotUsingProperty"))
				.containsExactly("[1] Customer{name='Luke', height=172}, Location{name='unknown'}",
					"[2] Customer{name='Luke', height=172}, Location{name='Tatooine'}",
					"[3] Customer{name='Yoda', height=66}, Location{name='unknown'}",
					"[4] Customer{name='Yoda', height=66}, Location{name='Tatooine'}");

	}

	private static String testSourceMethodName(TestDescriptor testDescriptor) {
		return testDescriptor
				.getSource()
				.filter(t -> t instanceof MethodSource)
				.map(t -> (MethodSource) t)
				.orElseThrow(() -> new RuntimeException("No method source"))
				.getMethodName();
	}

	@Nested
	class JsonSourceTests {

		@ParameterizedTest
		@JsonSource("[ { name: 'Luke', height: 172  }, { name: 'Yoda', height: 66 } ]")
		void deconstructCustomerFromArray(@Property("name") String name, @Property("height") int height) {
			assertThat(Collections.singleton(tuple(name, height))).containsAnyOf(tuple("Luke", 172), tuple("Yoda", 66));
		}

		@ParameterizedTest
		@JsonSource({ "{ name: 'Yoda', height: 66 }", "{ name: 'Luke', height: 172 }", })
		void deconstructCustomerMultipleValues(@Property("height") int height, @Property("name") String name) {
			assertThat(Collections.singleton(tuple(name, height))).containsAnyOf(tuple("Luke", 172), tuple("Yoda", 66));
		}

		@ParameterizedTest
		@JsonSource({ "{ name: 'Yoda', height: 66, location: { name: 'unknown' } }",
				"{ name: 'Luke', height: 172, location: { name: 'Tatooine' } }", })
		void deconstructCustomerMultipleLinesComplexType(@Property("name") String name,
				@Property("location") Location location) {

			assertThat(Collections.singleton(tuple(name, location.getName())))
					.containsAnyOf(tuple("Luke", "Tatooine"), tuple("Yoda", "unknown"));
		}

		@ParameterizedTest
		@JsonSource("[ { name: 'Luke', height: 172  }, { name: 'Yoda', height: 66 } ]")
		void singleCustomer(Customer customer) {
			assertThat(Collections.singleton(tuple(customer.getName(), customer.getHeight())))
					.containsAnyOf(tuple("Luke", 172), tuple("Yoda", 66));
		}

		@ParameterizedTest
		@JsonSource({ "{ name: 'Luke', height: 172 }", "{ name: 'Yoda', height: 66 }", })
		void singleCustomerName(@Property("name") String customerName) {
			assertThat(customerName).isIn("Luke", "Yoda");
		}

	}

	@Nested
	class JsonSourceCartesianTests {

		@CartesianTest
		@ReportEntry("{0},{1}")
		void extractPropertyFromArray(
				@JsonSource("[ { name: 'Luke', height: 172  }, { name: 'Yoda', height: 66 } ]") @Property("name") String name,
				@JsonSource("[ { name: 'Luke', height: 172  }, { name: 'Yoda', height: 66 } ]") @Property("height") int height) {
			assertThat(Collections.singleton(tuple(name, height)))
					.containsAnyOf(tuple("Luke", 172), tuple("Luke", 66), tuple("Yoda", 172), tuple("Yoda", 66));
		}

		@CartesianTest
		@ReportEntry("{0},{1}")
		void extractPropertyFromMultipleValues(
				@JsonSource({ "{ name: 'Yoda', height: 66 }",
						"{ name: 'Luke', height: 172 }", }) @Property("height") int height,
				@JsonSource({ "{ name: 'Yoda', height: 66 }",
						"{ name: 'Luke', height: 172 }", }) @Property("name") String name) {
			assertThat(Collections.singleton(tuple(name, height)))
					.containsAnyOf(tuple("Luke", 172), tuple("Luke", 66), tuple("Yoda", 172), tuple("Yoda", 66));
		}

		@CartesianTest
		@ReportEntry("{0},{1}")
		void extractPropertyMultipleLinesWithComplexType(@JsonSource({
				"{ name: 'Yoda', height: 66, location: { name: 'unknown' } }",
				"{ name: 'Luke', height: 172, location: { name: 'Tatooine' } }", }) @Property("name") String name,
				@JsonSource({ "{ name: 'Yoda', height: 66, location: { name: 'unknown' } }",
						"{ name: 'Luke', height: 172, location: { name: 'Tatooine' } }", }) @Property("location") Location location) {

			assertThat(Collections.singleton(tuple(name, location.getName())))
					.containsAnyOf(tuple("Luke", "Tatooine"), tuple("Luke", "unknown"), tuple("Yoda", "Tatooine"),
						tuple("Yoda", "unknown"));
		}

		@CartesianTest
		@ReportEntry("{0},{1}")
		void constructToParameterTypeWhenNotUsingProperty(
				@JsonSource("[ { name: 'Luke', height: 172  }, { name: 'Yoda', height: 66 } ]") Customer customer,
				@JsonSource({ "{ name: 'unknown' }", "{ name: 'Tatooine' }" }) Location location) {
		}

	}

	@Nested
	class InvalidJsonSourceTests {

		@Test
		void noValues() {
			ExecutionResults results = PioneerTestKit.executeTestMethod(InvalidJsonSource.class, "noValues");

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(PreconditionViolationException.class)
					.hasMessage("value must not be empty");
		}

		@Test
		void noValuesCartesian() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(InvalidJsonSource.class, "noValuesCartesian", Customer.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.getCause()
					.isInstanceOf(PreconditionViolationException.class)
					.hasMessage("value must not be empty");
		}

	}

	static class InvalidJsonSource {

		@JsonSource({})
		@ParameterizedTest
		void noValues() {

		}

		@CartesianTest
		void noValuesCartesian(@JsonSource({}) Customer customer) {

		}

	}

	// This class uses the Java Bean convention since the creation of the object is done by the Json Parsing library
	// We want to avoid adding specific Json Library annotations to this class, only to support Java record style
	static class Customer {

		private String name;
		private int height;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getHeight() {
			return height;
		}

		public void setHeight(int height) {
			this.height = height;
		}

		@Override
		public String toString() {
			return "Customer{" + "name='" + name + '\'' + ", height=" + height + '}';
		}

	}

	static class Location {

		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return "Location{" + "name='" + name + '\'' + '}';
		}

	}

}
