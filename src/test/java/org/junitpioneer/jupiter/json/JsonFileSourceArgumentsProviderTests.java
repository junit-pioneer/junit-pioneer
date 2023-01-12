/*
 * Copyright 2016-2022 the original author or authors.
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
 * Tests for {@link JsonFileSourceArgumentsProvider}
 */
class JsonFileSourceArgumentsProviderTests {

	private static final String TEST_RESOURCE_FOLDER = "build/resources/test/org/junitpioneer/jupiter/json/";
	private static final String JEDIS = TEST_RESOURCE_FOLDER + "jedis.json";
	private static final String YODA = TEST_RESOURCE_FOLDER + "yoda.json";
	private static final String LUKE = TEST_RESOURCE_FOLDER + "luke.json";

	@Test
	void assertAllValuesSupplied() {
		ExecutionResults results = PioneerTestKit.executeTestClass(JsonFileSourceTests.class);

		Map<String, List<String>> displayNames = results
				.dynamicallyRegisteredEvents()
				.map(Event::getTestDescriptor)
				.collect(groupingBy(JsonFileSourceArgumentsProviderTests::testSourceMethodName,
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
		ExecutionResults results = PioneerTestKit.executeTestClass(JsonFileSourceCartesianTests.class);

		Map<String, List<String>> displayNames = results
				.dynamicallyRegisteredEvents()
				.map(Event::getTestDescriptor)
				.collect(groupingBy(JsonFileSourceArgumentsProviderTests::testSourceMethodName,
					mapping(TestDescriptor::getDisplayName, toList())));

		assertThat(displayNames)
				.containsOnlyKeys("singleObject", "singleObjectProperty", "deconstructObjectsFromArray",
					"deconstructObjectsFromMultipleFiles", "deconstructObjectsFromMultipleFilesIntoComplexType");

		assertThat(displayNames.get("singleObject"))
				.containsExactly("[1] Jedi {name='Luke', height=172}", "[2] Jedi {name='Yoda', height=66}");

		assertThat(displayNames.get("singleObjectProperty")).containsExactly("[1] Luke", "[2] Yoda");

		assertThat(displayNames.get("deconstructObjectsFromArray"))
				.containsExactly("[1] Luke, 172", "[2] Luke, 66", "[3] Yoda, 172", "[4] Yoda, 66");

		//assertThat(displayNames.get("customDataLocation"))
		//		.containsExactly("[1] Snowspeeder, 4.5", "[2] Snowspeeder, 3", "[3] Imperial Speeder Bike, 4.5",
		//			"[4] Imperial Speeder Bike, 3");

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
	class JsonFileSourceTests {

		@ParameterizedTest
		@JsonFileSource(JEDIS)
		void singleObject(Jedi jedi) {
			assertThat(Set.of(tuple(jedi.getName(), jedi.getHeight())))
					.containsAnyOf(tuple("Luke", 172), tuple("Yoda", 66));
		}

		@ParameterizedTest
		@JsonFileSource(JEDIS)
		void singleObjectAttribute(@Property("name") String name) {
			assertThat(name).isIn("Luke", "Yoda");
		}

		@ParameterizedTest
		@JsonFileSource(JEDIS)
		void deconstructObjectsFromArray(@Property("name") String name, @Property("height") int height) {
			assertThat(Set.of(tuple(name, height))).containsAnyOf(tuple("Luke", 172), tuple("Yoda", 66));
		}

		@ParameterizedTest
		@JsonFileSource(value = LUKE, data = "vehicles")
		void customDataLocation(@Property("name") String name, @Property("length") double length) {
			assertThat(Set.of(tuple(name, length)))
					.containsAnyOf(tuple("Snowspeeder", 4.5), tuple("Imperial Speeder Bike", 3d));
		}

		@ParameterizedTest
		@JsonFileSource({ YODA, LUKE, })
		void deconstructObjectsFromMultipleFiles(@Property("height") int height, @Property("name") String name) {
			assertThat(Set.of(tuple(name, height))).containsAnyOf(tuple("Luke", 172), tuple("Yoda", 66));
		}

		@ParameterizedTest
		@JsonFileSource({ YODA, LUKE })
		void deconstructObjectsFromMultipleFilesIntoComplexType(@Property("name") String name,
				@Property("location") Location location) {
			assertThat(Set.of(tuple(name, location.getName())))
					.containsAnyOf(tuple("Luke", "Tatooine"), tuple("Yoda", "unknown"));
		}

	}

	@Nested
	class JsonFileSourceCartesianTests {

		@CartesianTest
		void singleObject(@JsonFileSource(JEDIS) Jedi jedi) {
			assertThat(Set.of(tuple(jedi.getName(), jedi.getHeight())))
					.containsAnyOf(tuple("Luke", 172), tuple("Yoda", 66));
		}

		@CartesianTest
		void singleObjectProperty(@JsonFileSource(JEDIS) @Property("name") String name) {
			assertThat(name).isIn("Luke", "Yoda");
		}

		@CartesianTest
		void deconstructObjectsFromArray(@JsonFileSource(JEDIS) @Property("name") String name,
				@JsonFileSource(JEDIS) @Property("height") int height) {
		}

		@CartesianTest
		@Disabled("Disabled until https://github.com/junit-pioneer/junit-pioneer/issues/577 is resolved")
		void customDataLocation(@JsonFileSource(value = LUKE, data = "vehicles") @Property("name") String name,
				@JsonFileSource(value = LUKE, data = "vehicles") @Property("length") double length) {
		}

		@CartesianTest
		void deconstructObjectsFromMultipleFiles(@JsonFileSource({ YODA, LUKE, }) @Property("height") int height,
				@JsonFileSource({ YODA, LUKE, }) @Property("name") String name) {
		}

		@CartesianTest
		void deconstructObjectsFromMultipleFilesIntoComplexType(
				@JsonFileSource({ YODA, LUKE }) @Property("name") String name,
				@JsonFileSource({ YODA, LUKE }) @Property("location") Location location) {
		}

	}

	@Nested
	class InvalidJsonSourceTests {

		@Test
		void noPaths() {
			ExecutionResults results = PioneerTestKit.executeTestMethod(InvalidJsonSourceTestCases.class, "noPaths");

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(PreconditionViolationException.class)
					.hasMessage("Value must not be empty");
		}

		@Test
		void emptyFilePath() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethod(InvalidJsonSourceTestCases.class, "emptyFilePath");

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(PreconditionViolationException.class)
					.hasMessage("File must not be null or blank");
		}

		@Test
		void nonExistentFile() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethod(InvalidJsonSourceTestCases.class, "nonExistentFile");

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(PreconditionViolationException.class)
					.hasMessageStartingWith("File does not exist: ");
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

		@JsonFileSource
		@ParameterizedTest
		void noPaths() {

		}

		@JsonFileSource({ YODA, "" })
		@ParameterizedTest
		void emptyFilePath() {

		}

		@JsonFileSource(TEST_RESOURCE_FOLDER + "dummy-jedi.json")
		@ParameterizedTest
		void nonExistentFile() {

		}

		@JsonFileSource(value = { YODA }, data = "dummy")
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
