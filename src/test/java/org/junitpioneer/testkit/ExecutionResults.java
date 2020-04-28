/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.testkit;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Events;

/**
 * Pioneers' class to handle JUnit Jupiter's {@link org.junit.platform.testkit.engine.EngineExecutionResults}.
 *
 * Instantiate with the static factory methods in {@link PioneerTestKit}.
 */
public class ExecutionResults {

	EngineExecutionResults executionResults;

	ExecutionResults(Class<?> testClass) {
		executionResults = EngineTestKit
				.engine("junit-jupiter")
				.selectors(DiscoverySelectors.selectClass(testClass))
				.execute();
	}

	ExecutionResults(Class<?> testClass, String testMethodName) {
		executionResults = EngineTestKit
				.engine("junit-jupiter")
				.selectors(DiscoverySelectors.selectMethod(testClass, testMethodName))
				.execute();
	}

	ExecutionResults(Class<?> testClass, String testMethodName, String methodParameterTypes) {
		executionResults = EngineTestKit
				.engine("junit-jupiter")
				.selectors(DiscoverySelectors.selectMethod(testClass, testMethodName, methodParameterTypes))
				.execute();
	}

	/**
	 * Get all recorded events.
	 */
	public Events allEvents() {
		return executionResults.all();
	}

	/**
	 * Get recorded dynamically registered events.
	 */
	public Events dynamicallyRegisteredEvents() {
		return executionResults.all().dynamicallyRegistered();
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
	 * @return number of all tests
	 */
	public long totalNumberOfTests() {
		return executionResults.tests().count();
	}

	/**
	 * Returns the number of started tests.
	 *
	 * @return number of started tests
	 */
	public long numberOfStartedTests() {
		return executionResults.tests().started().count();
	}

	/**
	 * Returns the number of failed tests.
	 *
	 * @return number of failed tests
	 */
	public long numberOfFailedTests() {
		return executionResults.tests().failed().count();
	}

	/**
	 * Returns the number of successful tests.
	 *
	 * @return number of successful tests
	 */
	public long numberOfSucceededTests() {
		return executionResults.tests().succeeded().count();
	}

	/**
	 * Returns the number of skipped tests.
	 *
	 * @return number of skipped tests
	 */
	public long numberOfSkippedTests() {
		return executionResults.tests().skipped().count();
	}

	/**
	 * Returns the number of aborted tests.
	 *
	 * @return number of aborted events results
	 */
	public long numberOfAbortedTests() {
		return executionResults.tests().aborted().count();
	}

	/**
	 * Returns the number of failed containers.
	 *
	 * @return number of failed containers
	 */
	public long numberOfFailedContainers() {
		return executionResults.containers().failed().count();
	}

	/**
	 * Returns the number of dynamically registered tests.
	 *
	 * @return number of dynamically registered tests
	 */
	public long numberOfDynamicRegisteredTests() {
		return executionResults.tests().dynamicallyRegistered().count();
	}

	/**
	 * Returns the number of published report entries.
	 *
	 * @return number of published report entries
	 */
	public long numberOfPublishedReportEntries() {
		return executionResults.all().reportingEntryPublished().count();
	}

	public List<Map<String, String>> reportEntries() {
		return executionResults
				.all()
				.reportingEntryPublished()
				.stream()
				.map(event -> event.getPayload(ReportEntry.class))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(ReportEntry::getKeyValuePairs)
				.collect(toList());
	}

	/**
	 * Returns the published report entries of all tests.
	 *
	 * @return published report entries of all tests
	 */
	public List<Map<String, String>> publishedTestReportEntries() {
		return executionResults
				.tests()
				.reportingEntryPublished()
				.stream()
				.map(executionEvent -> executionEvent.getPayload(ReportEntry.class))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(ReportEntry::getKeyValuePairs)
				.collect(toList());
	}

}
