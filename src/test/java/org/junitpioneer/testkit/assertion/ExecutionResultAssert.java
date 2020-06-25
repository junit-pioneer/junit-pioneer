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
 * An intermediary interface for choosing what you want to assert (except if you want to assert that no report entries were published).
 */
public interface ExecutionResultAssert {

	ReportEntryAssert hasNumberOfReportEntries(int expected);

	ReportEntryAssert hasSingleReportEntry();

	void hasNoReportEntries();

	TestCaseAssert hasSingleStartedTest();

	FailureAssert hasSingleFailedTest();

	void hasSingleAbortedTest();

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
