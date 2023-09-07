/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.json;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.testkit.engine.Event;
import org.junitpioneer.jupiter.cartesian.CartesianTest;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

/**
 * Tests for {@link JsonClasspathSourceArgumentsProvider}
 */
class JsonClasspathSourceArgumentsProviderTests {

	private static final String JEDIS = "org/junitpioneer/jupiter/json/jedis.json";
	private static final String YODA = "org/junitpioneer/jupiter/json/yoda.json";
	private static final String LUKE = "org/junitpioneer/jupiter/json/luke.json";

	@Test
	void assertAllValuesSupplied() {
		ExecutionResults results = PioneerTestKit.executeTestClass(JsonClasspathSourceTests.class);

		Map<String, List<String>> displayNames = results
				.dynamicallyRegisteredEvents()
				.map(Event::getTestDescriptor)
				.collect(groupingBy(JsonClasspathSourceArgumentsProviderTests::testSourceMethodName,
					mapping(TestDescriptor::getDisplayName, toList())));

		assertThat(displayNames)
				.containsOnlyKeys("singleObject", "singleObjectAttribute", "deconstructObjectsFromArray",
					"customDataLocation", "deconstructObjectsFromMultipleFiles",
					"deconstructObjectsFromMultipleFilesIntoComplexType");

		assertThat(displayNames.get("singleObject"))
				.containsExactly("[1] Jedi {name='Luke', height=172}", "[2] Jedi {name='Yoda', height=66}");

		assertThat(displayNames.get("singleObjectAttribute")).containsExactly("[1] Luke", "[2] Yoda");

		assertThat(displayNames.get("deconstructObjectsFromArray")).containsExactly("[1] Luke, 172", "[2] Yoda, 66");

		assertThat(displayNames.get("customDataLocation"))
				.containsExactly("[1] Snowspeeder, 4.5", "[2] Imperial Speeder Bike, 3");

		assertThat(displayNames.get("deconstructObjectsFromMultipleFiles"))
				.containsExactly("[1] 66, Yoda", "[2] 172, Luke");

		assertThat(displayNames.get("deconstructObjectsFromMultipleFilesIntoComplexType"))
				.containsExactly("[1] Yoda, Location {name='unknown'}", "[2] Luke, Location {name='Tatooine'}");
	}

	@Test
	void assertAllCartesianValuesSupplied() {
		ExecutionResults results = PioneerTestKit.executeTestClass(JsonClasspathSourceCartesianTests.class);

		Map<String, List<String>> displayNames = results
				.dynamicallyRegisteredEvents()
				.map(Event::getTestDescriptor)
				.collect(groupingBy(JsonClasspathSourceArgumentsProviderTests::testSourceMethodName,
					mapping(TestDescriptor::getDisplayName, toList())));

		assertThat(displayNames)
				.containsOnlyKeys("singleObject", "singleObjectProperty", "deconstructObjectsFromArray",
					"deconstructObjectsFromMultipleFiles", "deconstructObjectsFromMultipleFilesIntoComplexType");

		assertThat(displayNames.get("singleObject"))
				.containsExactly("[1] Jedi {name='Luke', height=172}", "[2] Jedi {name='Yoda', height=66}");

		assertThat(displayNames.get("singleObjectProperty")).containsExactly("[1] Luke", "[2] Yoda");

		assertThat(displayNames.get("deconstructObjectsFromArray"))
				.containsExactly("[1] Luke, 172", "[2] Luke, 66", "[3] Yoda, 172", "[4] Yoda, 66");

		assertThat(displayNames.get("deconstructObjectsFromMultipleFiles"))
				.containsExactly("[1] 66, Yoda", "[2] 66, Luke", "[3] 172, Yoda", "[4] 172, Luke");

		assertThat(displayNames.get("deconstructObjectsFromMultipleFilesIntoComplexType"))
				.containsExactly("[1] Yoda, Location {name='unknown'}", "[2] Yoda, Location {name='Tatooine'}",
					"[3] Luke, Location {name='unknown'}", "[4] Luke, Location {name='Tatooine'}");
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
	class JsonClasspathSourceTests {

		@ParameterizedTest
		@JsonClasspathSource(JEDIS)
		void singleObject(Jedi jedi) {
			assertThat(Set.of(tuple(jedi.getName(), jedi.getHeight())))
					.containsAnyOf(tuple("Luke", 172), tuple("Yoda", 66));
		}

		@ParameterizedTest
		@JsonClasspathSource(JEDIS)
		void singleObjectAttribute(@Property("name") String name) {
			assertThat(name).isIn("Luke", "Yoda");
		}

		@ParameterizedTest
		@JsonClasspathSource(JEDIS)
		void deconstructObjectsFromArray(@Property("name") String name, @Property("height") int height) {
			assertThat(Set.of(tuple(name, height))).containsAnyOf(tuple("Luke", 172), tuple("Yoda", 66));
		}

		@ParameterizedTest
		@JsonClasspathSource(value = LUKE, data = "vehicles")
		void customDataLocation(@Property("name") String name, @Property("length") double length) {
			assertThat(Set.of(tuple(name, length)))
					.containsAnyOf(tuple("Snowspeeder", 4.5), tuple("Imperial Speeder Bike", 3d));
		}

		@ParameterizedTest
		@JsonClasspathSource({ YODA, LUKE, })
		void deconstructObjectsFromMultipleFiles(@Property("height") int height, @Property("name") String name) {
			assertThat(Set.of(tuple(name, height))).containsAnyOf(tuple("Luke", 172), tuple("Yoda", 66));
		}

		@ParameterizedTest
		@JsonClasspathSource({ YODA, LUKE })
		void deconstructObjectsFromMultipleFilesIntoComplexType(@Property("name") String name,
				@Property("location") Location location) {
			assertThat(Set.of(tuple(name, location.getName())))
					.containsAnyOf(tuple("Luke", "Tatooine"), tuple("Yoda", "unknown"));
		}

	}

	@Nested
	class JsonClasspathSourceCartesianTests {

		@CartesianTest
		void singleObject(@JsonClasspathSource(JEDIS) Jedi jedi) {
			assertThat(Set.of(tuple(jedi.getName(), jedi.getHeight())))
					.containsAnyOf(tuple("Luke", 172), tuple("Yoda", 66));
		}

		@CartesianTest
		void singleObjectProperty(@JsonClasspathSource(JEDIS) @Property("name") String name) {
			assertThat(name).isIn("Luke", "Yoda");
		}

		@CartesianTest
		void deconstructObjectsFromArray(@JsonClasspathSource(JEDIS) @Property("name") String name,
				@JsonClasspathSource(JEDIS) @Property("height") int height) {
		}

		@CartesianTest
		@Disabled("Disabled until https://github.com/junit-pioneer/junit-pioneer/issues/577 is resolved")
		void customDataLocation(@JsonClasspathSource(value = LUKE, data = "vehicles") @Property("name") String name,
				@JsonClasspathSource(value = LUKE, data = "vehicles") @Property("length") double length) {
		}

		@CartesianTest
		void deconstructObjectsFromMultipleFiles(@JsonClasspathSource({ YODA, LUKE, }) @Property("height") int height,
				@JsonClasspathSource({ YODA, LUKE, }) @Property("name") String name) {
		}

		@CartesianTest
		void deconstructObjectsFromMultipleFilesIntoComplexType(
				@JsonClasspathSource({ YODA, LUKE }) @Property("name") String name,
				@JsonClasspathSource({ YODA, LUKE }) @Property("location") Location location) {
		}

	}

	@Nested
	class InvalidJsonSourceTests {

		@Test
		void noResources() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethod(InvalidJsonSourceTestCases.class, "noResources");

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(PreconditionViolationException.class)
					.hasMessage("Value must not be empty");
		}

		@Test
		void emptyClasspathResource() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethod(InvalidJsonSourceTestCases.class, "emptyClasspathResource");

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(PreconditionViolationException.class)
					.hasMessage("Classpath resource must not be null or blank");
		}

		@Test
		void missingClasspathResource() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethod(InvalidJsonSourceTestCases.class, "missingClasspathResource");

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(PreconditionViolationException.class)
					.hasMessage("Classpath resource [dummy-jedi.json] does not exist");
		}

		@Test
		void dataLocationMissing() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethod(InvalidJsonSourceTestCases.class, "dataLocationMissing");

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(PreconditionViolationException.class)
					.hasMessageContainingAll("Node ", "does not have data element at dummy");
		}

	}

	static class InvalidJsonSourceTestCases {

		@JsonClasspathSource
		@ParameterizedTest
		void noResources() {

		}

		@JsonClasspathSource({ YODA, "" })
		@ParameterizedTest
		void emptyClasspathResource() {

		}

		@JsonClasspathSource("dummy-jedi.json")
		@ParameterizedTest
		void missingClasspathResource() {

		}

		@JsonClasspathSource(value = { YODA }, data = "dummy")
		@ParameterizedTest
		void dataLocationMissing() {

		}

	}

	// This class uses the Java Bean convention since the creation of the object is done by the Json Parsing library
	// We want to avoid adding specific Json Library annotations to this class, only to support Java record style
	static class Jedi {

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
			return "Jedi {" + "name='" + name + '\'' + ", height=" + height + '}';
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
			return "Location {" + "name='" + name + '\'' + '}';
		}

	}

}
