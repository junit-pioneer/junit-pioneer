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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.testkit.engine.Event;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

/**
 * Tests for {@link JsonFileArgumentsProvider}
 */
class JsonFileSourceArgumentsProviderTests {

	@Test
	void assertAllValuesSupplied() {
		ExecutionResults results = PioneerTestKit.executeTestClass(JsonFileSourceTestCases.class);

		Map<String, List<String>> displayNames = results
				.dynamicallyRegisteredEvents()
				.map(Event::getTestDescriptor)
				.collect(Collectors
						.groupingBy(JsonFileSourceArgumentsProviderTests::testSourceMethodName,
							Collectors.mapping(TestDescriptor::getDisplayName, Collectors.toList())));

		assertThat(displayNames)
				.containsOnlyKeys("deconstructCustomerFromArray", "deconstructCustomerMultipleFiles",
					"deconstructCustomerMultipleFilesComplexType", "singleCustomer", "singleCustomerName",
					"customDataLocation");

		assertThat(displayNames.get("deconstructCustomerFromArray")).containsExactly("[1] Luke, 172", "[2] Yoda, 66");

		assertThat(displayNames.get("deconstructCustomerMultipleFiles"))
				.containsExactly("[1] 66, Yoda", "[2] 172, Luke");

		assertThat(displayNames.get("deconstructCustomerMultipleFilesComplexType"))
				.containsExactly("[1] Yoda, Location{name='unknown'}", "[2] Luke, Location{name='Tatooine'}");

		assertThat(displayNames.get("singleCustomer"))
				.containsExactly("[1] Customer{name='Luke', height=172}", "[2] Customer{name='Yoda', height=66}");

		assertThat(displayNames.get("singleCustomerName")).containsExactly("[1] Luke", "[2] Yoda");

		assertThat(displayNames.get("customDataLocation"))
				.containsExactly("[1] Snowspeeder, 4.5", "[2] Imperial Speeder Bike, 3");

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
	class JsonFileSourceTestCases {

		@ParameterizedTest
		@JsonFileSource(resources = "org/junitpioneer/jupiter/json/customers.json")
		void deconstructCustomerFromArray(@Param("name") String name, @Param("height") int height) {
			assertThat(Collections.singleton(tuple(name, height))).containsAnyOf(tuple("Luke", 172), tuple("Yoda", 66));
		}

		@ParameterizedTest
		@JsonFileSource(resources = { "org/junitpioneer/jupiter/json/customer-yoda.json",
				"org/junitpioneer/jupiter/json/customer-luke.json", })
		void deconstructCustomerMultipleFiles(@Param("height") int height, @Param("name") String name) {
			assertThat(Collections.singleton(tuple(name, height))).containsAnyOf(tuple("Luke", 172), tuple("Yoda", 66));
		}

		@ParameterizedTest
		@JsonFileSource(resources = { "org/junitpioneer/jupiter/json/customer-yoda.json",
				"org/junitpioneer/jupiter/json/customer-luke.json", })
		void deconstructCustomerMultipleFilesComplexType(@Param("name") String name,
				@Param("location") Location location) {

			assertThat(Collections.singleton(tuple(name, location.getName())))
					.containsAnyOf(tuple("Luke", "Tatooine"), tuple("Yoda", "unknown"));
		}

		@ParameterizedTest
		@JsonFileSource(resources = "org/junitpioneer/jupiter/json/customers.json")
		void singleCustomer(Customer customer) {
			assertThat(Collections.singleton(tuple(customer.getName(), customer.getHeight())))
					.containsAnyOf(tuple("Luke", 172), tuple("Yoda", 66));
		}

		@ParameterizedTest
		@JsonFileSource(resources = "org/junitpioneer/jupiter/json/customers.json")
		void singleCustomerName(@Param("name") String customerName) {
			assertThat(customerName).isIn("Luke", "Yoda");
		}

		@ParameterizedTest
		@JsonFileSource(resources = { "org/junitpioneer/jupiter/json/customer-luke.json" }, data = "vehicles")
		void customDataLocation(@Param("name") String name, @Param("length") double length) {
			assertThat(Collections.singleton(tuple(name, length)))
					.containsAnyOf(tuple("Snowspeeder", 4.5), tuple("Imperial Speeder Bike", 3d));
		}

	}

	@Nested
	class InvalidJsonSourceTestCases {

		@Test
		void noFilesOrResources() {
			ExecutionResults results = PioneerTestKit.executeTestMethod(InvalidJsonSource.class, "noFilesOrResources");

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(PreconditionViolationException.class)
					.hasMessage("Resources or files must not be empty");
		}

		@Test
		void emptyClasspathResource() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethod(InvalidJsonSource.class, "emptyClasspathResource");

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(PreconditionViolationException.class)
					.hasMessage("Classpath resource must not be null or blank");
		}

		@Test
		void missingClasspathResource() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethod(InvalidJsonSource.class, "missingClasspathResource");

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(PreconditionViolationException.class)
					.hasMessage("Classpath resource [dummy-customer.json] does not exist");
		}

		@Test
		void dataLocationMissing() {
			ExecutionResults results = PioneerTestKit.executeTestMethod(InvalidJsonSource.class, "dataLocationMissing");

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(PreconditionViolationException.class)
					.hasMessageContainingAll("Node ", "does not have data element at dummy");
		}

	}

	static class InvalidJsonSource {

		@JsonFileSource
		@ParameterizedTest
		void noFilesOrResources() {

		}

		@JsonFileSource(resources = { "org/junitpioneer/jupiter/json/customer-yoda.json", "" })
		@ParameterizedTest
		void emptyClasspathResource() {

		}

		@JsonFileSource(resources = "dummy-customer.json")
		@ParameterizedTest
		void missingClasspathResource() {

		}

		@JsonFileSource(resources = { "org/junitpioneer/jupiter/json/customer-yoda.json", }, data = "dummy")
		@ParameterizedTest
		void dataLocationMissing() {

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
