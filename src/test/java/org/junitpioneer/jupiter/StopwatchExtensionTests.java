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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;
import org.junitpioneer.testkit.assertion.PioneerAssert;

@DisplayName("Stopwatch extension ")
public class StopwatchExtensionTests {

	@DisplayName("should be executed with annotation on class level and report an entry for test method")
	@Test
	void runClassLevelAnnotationTest() {

		ExecutionResults results = PioneerTestKit
				.executeTestClass(StopwatchExtensionTests.ClassLevelAnnotationTest.class);

		PioneerAssert.assertThat(results).hasNumberOfReportEntries(1);

		String methodName = "stopwatchExtensionShouldBeExecutedWithAnnotationOnClassLevel";
		assertStringStartWithUnitAndContainsName(results, methodName);

	}

	@DisplayName("should be executed with annotation on class level and test method and report an entry for test method")
	@Test
	void runClassAndMethodLevelAnnotationTest() {
		String methodName = "stopwatchExtensionShouldBeExecutedWithAnnotationOnClassAndMethodLevel";

		ExecutionResults results = PioneerTestKit
				.executeTestClass(StopwatchExtensionTests.ClassAndMethodLevelAnnotationTest.class);
		PioneerAssert.assertThat(results).hasNumberOfReportEntries(1);

		assertStringStartWithUnitAndContainsName(results, methodName);
	}

	@DisplayName("should be executed with annotation on test method and report an entry for test method")
	@Test
	void runMethodLevelAnnotationTest() {
		String methodName = "stopwatchExtensionShouldBeExecutedOnWithAnnotationOnMethodLevel";

		ExecutionResults results = PioneerTestKit
				.executeTestMethod(StopwatchExtensionTests.MethodLevelAnnotationTest.class, methodName);
		PioneerAssert.assertThat(results).hasNumberOfReportEntries(1);

		assertStringStartWithUnitAndContainsName(results, methodName);
	}

	@DisplayName("should not be executed and therefore no entry should be published")
	@Test
	void runAnnotationTest() {
		String methodName = "stopwatchExtensionShouldNotBeExecuted";

		ExecutionResults results = PioneerTestKit
				.executeTestMethod(StopwatchExtensionTests.NonAnnotationTest.class, methodName);
		PioneerAssert.assertThat(results).hasNumberOfReportEntries(0);

	}

	private void assertStringStartWithUnitAndContainsName(ExecutionResults results, String methodName) {
		PioneerAssert.assertThat(results).hasNumberOfReportEntries(1).andThen(entry -> {
			assertThat(entry.getKey()).isEqualTo(StopwatchExtension.STORE_KEY);
			assertThat(entry.getValue())
					.matches(String.format("Execution of '%s\\(\\)' took \\[[0-9]*\\] ms.", methodName));
		});
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
	 * Inner test class for testing a not annotated method / class annotation.
	 */
	static class NonAnnotationTest {

		@Test
		void stopwatchExtensionShouldNotBeExecuted() {
		}

	}

}
