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

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

@DisplayName("Stopwatch extension ")
public class StopwatchExtensionTests {

	@DisplayName("should be executed with annotation on class level and report an entry for test method")
	@Test
	void runClassLevelAnnotationTest() {

		ExecutionResults results = PioneerTestKit
				.executeTestClass(StopwatchExtensionTests.ClassLevelAnnotationTest.class);

		org.junitpioneer.testkit.assertion.PioneerAssert.assertThat(results).hasNumberOfReportEntries(1);

		//		List<Map<String, String>> reportEntries = results.
		//
		//
		//		Map<String, String> classEntry = results.allEvents().reportingEntryPublished().get(0);
		//		String methodName = "stopwatchExtensionShouldBeExecutedWithAnnotationOnClassLevel";
		//		assertStringStartWithUnitAndContainsName(classEntry, methodName);

	}

	@DisplayName("should be executed with annotation on class level and test method and report an entry for test method")
	@Test
	void runClassAndMethodLevelAnnotationTest() {
		String methodName = "stopwatchExtensionShouldBeExecutedWithAnnotationOnClassAndMethodLevel";

		ExecutionResults results = PioneerTestKit
				.executeTestClass(StopwatchExtensionTests.ClassAndMethodLevelAnnotationTest.class);
		org.junitpioneer.testkit.assertion.PioneerAssert.assertThat(results).hasNumberOfReportEntries(1);

		//		List<Map<String, String>> reportEntries = PioneerTestKit
		//				.executeTestClass(StopwatchExtensionTests.ClassAndMethodLevelAnnotationTest.class)
		//				.publishedTestReportEntries();
		//		assertThat(reportEntries).hasSize(1);
		//
		//		Map<String, String> methodEntry = reportEntries.get(0);
		//		assertStringStartWithUnitAndContainsName(methodEntry, methodName);
	}

	@DisplayName("should be executed with annotation on test method and report an entry for test method")
	@Test
	void runMethodLevelAnnotationTest() {
		String methodName = "stopwatchExtensionShouldBeExecutedOnWithAnnotationOnMethodLevel";

		ExecutionResults results = PioneerTestKit
				.executeTestMethod(StopwatchExtensionTests.MethodLevelAnnotationTest.class, methodName);
		org.junitpioneer.testkit.assertion.PioneerAssert.assertThat(results).hasNumberOfReportEntries(1);

		//		List<Map<String, String>> reportEntries = PioneerTestKit
		//				.executeTestMethod(StopwatchExtensionTests.MethodLevelAnnotationTest.class, methodName)
		//				.publishedTestReportEntries();
		//
		//		Map<String, String> reportEntry = reportEntries.get(0);
		//		assertStringStartWithUnitAndContainsName(reportEntry, methodName);
	}

	@DisplayName("should not be executed and therefore no entry should be published")
	@Test
	void runAnnotationTest() {
		String methodName = "stopwatchExtensionShouldNotBeExecuted";

		ExecutionResults results = PioneerTestKit
				.executeTestMethod(StopwatchExtensionTests.MethodLevelAnnotationTest.class, methodName);
		org.junitpioneer.testkit.assertion.PioneerAssert.assertThat(results).hasNumberOfReportEntries(0);

	}

	private void assertStringStartWithUnitAndContainsName(Map<String, String> reportEntry, String name) {
		assertThat(reportEntry).hasSize(1);

		String result = reportEntry.get("stopwatch");
		assertThat(result).isNotNull();

		String startsWith = String.format("Execution of '%s()' took [", name);

		assertThat(result).startsWith(startsWith);
		assertThat(result).endsWith("] ms.");
	}

	/**
	 * Inner test class for testing the class level annotation.
	 */
	@Stopwatch
	static class ClassLevelAnnotationTest {

		@Test
		void stopwatchExtensionShouldBeExecutedWithAnnotationOnClassLevel() {
		}

	}

	/**
	 * Inner test class for testing the method level annotation.
	 */
	static class MethodLevelAnnotationTest {

		@Stopwatch
		@Test
		void stopwatchExtensionShouldBeExecutedOnWithAnnotationOnMethodLevel() {
		}

	}

	/**
	 * Inner test class for testing the class level annotation.
	 */
	@Stopwatch
	static class ClassAndMethodLevelAnnotationTest {

		@Stopwatch
		@Test
		void stopwatchExtensionShouldBeExecutedWithAnnotationOnClassAndMethodLevel() {
		}

	}

	/**
	 * Inner test class for testing a not annotated method / classs annotation.
	 */
	static class NonAnnotationTest {

		@Test
		void stopwatchExtensionShouldNotBeExecuted() {
		}

	}

}
