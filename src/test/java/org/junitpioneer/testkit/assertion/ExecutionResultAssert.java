/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.testkit.assertion;

/**
 * Base interface for all {@link org.junitpioneer.testkit.ExecutionResults} assertions.
 */
public interface ExecutionResultAssert {

	/**
	 * Asserts that the expected number of report entries were published across all executed tests.
	 * @param expected the number of report entries expected to be published
	 * @return a {@link ReportEntryAssert} for further assertions.
	 */
	ReportEntryAssert hasNumberOfReportEntries(int expected);

	/**
	 * Asserts that exactly one report entry was published across all executed tests.
	 * @return a {@link ReportEntryAssert} for further assertions.
	 */
	ReportEntryAssert hasSingleReportEntry();

	/**
	 * Asserts that no report entries were published across all executed tests.
	 */
	void hasNoReportEntries();

	TestCaseAssert hasSingleStartedTest();

	FailureAssert hasSingleFailedTest();

	void hasSingleAbortedTest();

	/**
	 * Asserts that there was exactly one successful test.
	 * <p>
	 * This is a convenience method that should be used by itself.
	 * If you want to assert an entire test suite with multiple tests,
	 * you should use {@code hasNumberOfSucceededTests(1)} (even if
	 * it is your last method) for better clarity.
	 */
	void hasSingleSucceededTest();

	void hasSingleSkippedTest();

	TestCaseAssert hasSingleDynamicallyRegisteredTest();

	TestCaseAssert hasSingleStartedContainer();

	FailureAssert hasSingleFailedContainer();

	void hasSingleAbortedContainer();

	void hasSingleSucceededContainer();

	void hasSingleSkippedContainer();

	TestCaseAssert hasSingleDynamicallyRegisteredContainer();

	ExecutionResultAssert hasNumberOfStartedTests(int expected);

	ExecutionResultAssert hasNumberOfFailedTests(int expected);

	ExecutionResultAssert hasNumberOfAbortedTests(int expected);

	ExecutionResultAssert hasNumberOfSucceededTests(int expected);

	ExecutionResultAssert hasNumberOfSkippedTests(int expected);

	ExecutionResultAssert hasNumberOfDynamicallyRegisteredTests(int expected);

	ExecutionResultAssert hasNumberOfStartedContainers(int expected);

	ExecutionResultAssert hasNumberOfFailedContainers(int expected);

	ExecutionResultAssert hasNumberOfAbortedContainers(int expected);

	ExecutionResultAssert hasNumberOfSucceededContainers(int expected);

	ExecutionResultAssert hasNumberOfSkippedContainers(int expected);

	ExecutionResultAssert hasNumberOfDynamicallyRegisteredContainers(int expected);

}
