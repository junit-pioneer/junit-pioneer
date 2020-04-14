/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.platform.testkit.engine;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Events;

/**
 * Pioneers' class to handle JUnit Jupiter's {@link org.junit.platform.testkit.engine.EngineExecutionResults}.
 *
 * Instantiate with the static factory methods in {@link PioneerTestKit}.
 */
public class PioneerEngineExecutionResults {

	EngineExecutionResults executionResults;

	PioneerEngineExecutionResults(Class<?> testClass) {
		executionResults = EngineTestKit
				.engine("junit-jupiter")
				.selectors(DiscoverySelectors.selectClass(testClass))
				.execute();
	}

	PioneerEngineExecutionResults(Class<?> testClass, String testMethodName) {
		executionResults = EngineTestKit
				.engine("junit-jupiter")
				.selectors(DiscoverySelectors.selectMethod(testClass, testMethodName))
				.execute();
	}

	/**
	 * Get all recorded events.
	 */
	public Events allEvents() {
		return executionResults.all();
	}

	/**
	 * Get recorded events for containers.
	 *
	 * <p>In this context, the word "container" applies to {@link org.junit.platform.engine.TestDescriptor
	 * TestDescriptors} that return {@code true} from {@link org.junit.platform.engine.TestDescriptor#isContainer()}.</p>
	 */
	public Events containerEvents() {
		return executionResults.containers();
	}

	/**
	 * Get recorded events for tests.
	 *
	 * <p>In this context, the word "test" applies to {@link org.junit.platform.engine.TestDescriptor
	 * TestDescriptors} that return {@code true} from {@link org.junit.platform.engine.TestDescriptor#isTest()}.</p>
	 */
	public Events testEvents() {
		return executionResults.tests();
	}

	/**
	 * Returns the number of all tests.
	 *
	 * @return Number of all tests
	 */
	public long getTotalNumberOfTests() {
		return executionResults.tests().count();
	}

	/**
	 * Returns the number of failed tests.
	 *
	 * @return Number of failed tests
	 */
	public long getNumberOfFailedTests() {
		return executionResults.all().failed().count();
	}

	/**
	 * Returns the number of successful tests.
	 *
	 * @return Number of successful tests
	 */
	public long getNumberOfSucceededEvents() {
		return executionResults.all().succeeded().count();
	}

	/**
	 * Returns the number of skipped tests.
	 *
	 * @return Number of skipped tests
	 */
	public long getNumberOfSkippedEvents() {
		return executionResults.all().skipped().count();
	}

	/**
	 * Returns the number of aborted tests.
	 *
	 * @return Number of aborted events results
	 */
	public long getNumberOfAbortedEvents() {
		return executionResults.all().aborted().count();
	}

	/**
	 * Returns the message of the first failed event.
	 * This can be used if you expect a test to fail with an exception and want to check the exception message.
	 *
	 * @return Message of the first failed event.
	 */
	public String getFirstFailuresThrowableMessage() {
		return executionResults
				.all()
				.failed()
				.stream()
				.findFirst()
				.orElseThrow(AssertionError::new)
				.getPayload(TestExecutionResult.class)
				.flatMap(TestExecutionResult::getThrowable)
				.orElseThrow(AssertionError::new)
				.getMessage();
	}

	/**
	 * Returns the published report entries of all tests.
	 *
	 * @return published report entries of all tests
	 */
	public List<Map<String, String>> getPublishedTestReportEntries() {
		return executionResults
				.tests()
				.reportingEntryPublished()
				.stream()
				.map(executionEvent -> executionEvent.getPayload(org.junit.platform.engine.reporting.ReportEntry.class))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(org.junit.platform.engine.reporting.ReportEntry::getKeyValuePairs)
				.collect(toList());
	}

}
