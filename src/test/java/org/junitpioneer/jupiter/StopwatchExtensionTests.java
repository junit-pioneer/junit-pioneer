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
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junitpioneer.AbstractPioneerTestEngineTests;
import org.junitpioneer.jupiter.enumerations.TestunitEnum;
import org.junitpioneer.jupiter.utils.EventRecorderUtils;

@DisplayName("Stopwatch extension tests")
public class StopwatchExtensionTests extends AbstractPioneerTestEngineTests {

	@Test
	void runClassLevelAnnotationTest() {
		ExecutionEventRecorder eventRecorder = executeTests(StopwatchExtensionTests.ClassLevelAnnotationTest.class,
			"stopwatchExtensionShouldBeExecutedWithAnnotationOnClassLevel");

		Map<String, String> reportEntry = EventRecorderUtils.getFirstReportEntry(eventRecorder);
		assertThat(reportEntry).hasSize(1);

		String result = reportEntry.get("stopwatch");
		assertThat(result).isNotNull();
		assertThat(result).startsWith(TestunitEnum.CLASS.name());
		assertThat(result).contains("ClassLevelAnnotationTest");
		assertThat(result).endsWith("ms.");
	}

	@Test
	void runMethodLevelAnnotationTest() {
		ExecutionEventRecorder eventRecorder = executeTests(StopwatchExtensionTests.MethodLevelAnnotationTest.class,
			"stopwatchExtensionShouldBeExecutedOnWithAnnotationOnMethodLevel");

		Map<String, String> reportEntry = EventRecorderUtils.getFirstReportEntry(eventRecorder);
		assertThat(reportEntry).hasSize(1);

		String result = reportEntry.get("stopwatch");
		assertThat(result).isNotNull();
		assertThat(result).startsWith(TestunitEnum.TEST.name());
		assertThat(result).contains("stopwatchExtensionShouldBeExecutedOnWithAnnotationOnMethodLevel");
		assertThat(result).endsWith("ms.");
	}

	@Test
	void runNonLevelAnnotationTest() {
		ExecutionEventRecorder eventRecorder = executeTests(StopwatchExtensionTests.NonAnnotationTest.class,
			"stopwatchExtensionShouldNotBeExecuted");

		Map<String, String> reportEntry = EventRecorderUtils.getFirstReportEntry(eventRecorder);
		assertThat(reportEntry).isEmpty();
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
	 * Inner test class for testing a not annotated method / classs annotation.
	 */
	static class NonAnnotationTest {

		@Test
		void stopwatchExtensionShouldNotBeExecuted() {
		}

	}

}
