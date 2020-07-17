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

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.assertion.reportentry.ReportEntryValueAssert;
import org.junitpioneer.testkit.assertion.single.TestCaseFailureAssert;
import org.junitpioneer.testkit.assertion.single.TestCaseStartedAssert;
import org.junitpioneer.testkit.assertion.suite.TestSuiteAssert;
import org.junitpioneer.testkit.assertion.suite.TestSuiteFailureAssert;
import org.junitpioneer.testkit.assertion.suite.TestSuiteFailureMessageAssert;

public class PioneerAssert extends AbstractAssert<PioneerAssert, ExecutionResults> implements ExecutionResultAssert, TestSuiteFailureAssert {

	public static ExecutionResultAssert assertThat(ExecutionResults actual) {
		return new PioneerAssert(actual);
	}

	private PioneerAssert(ExecutionResults actual) {
		super(actual, PioneerAssert.class);
	}

	@Override
	public ReportEntryValueAssert hasNumberOfReportEntries(int expected) {
		List<Map<String, String>> entries = reportEntries();
		Assertions.assertThat(entries).hasSize(expected);
		Integer[] ones = IntStream.generate(() -> 1).limit(expected).boxed().toArray(Integer[]::new);
		Assertions.assertThat(entries).extracting(Map::size).containsExactly(ones);

		List<Map.Entry<String, String>> entryList = entries
				.stream()
				.flatMap(map -> map.entrySet().stream())
				.collect(Collectors.toList());

		return new ReportEntryAssertBase(entryList, expected);
	}

	@Override
	public ReportEntryValueAssert hasSingleReportEntry() {
		return hasNumberOfReportEntries(1);
	}

	@Override
	public void hasNoReportEntries() {
		Assertions.assertThat(reportEntries()).isEmpty();
	}

	@Override
	public TestCaseStartedAssert hasSingleStartedTest() {
		return assertSingleTest(actual.testEvents().started().count());
	}

	@Override
	public TestCaseFailureAssert hasSingleFailedTest() {
		return assertSingleTest(actual.testEvents().failed().count());
	}

	@Override
	public void hasSingleAbortedTest() {
		Assertions.assertThat(actual.testEvents().aborted().count()).isEqualTo(1);
	}

	@Override
	public void hasSingleSucceededTest() {
		Assertions.assertThat(actual.testEvents().succeeded().count()).isEqualTo(1);
	}

	@Override
	public void hasSingleSkippedTest() {
		Assertions.assertThat(actual.testEvents().skipped().count()).isEqualTo(1);
	}

	@Override
	public TestCaseStartedAssert hasSingleDynamicallyRegisteredTest() {
		return assertSingleTest(actual.testEvents().dynamicallyRegistered().count());
	}

	private TestCaseAssertBase assertSingleTest(long numberOfTestsWithOutcome) {
		Assertions.assertThat(numberOfTestsWithOutcome).isEqualTo(1);
		return new TestCaseAssertBase(actual.testEvents());
	}

	@Override
	public TestCaseStartedAssert hasSingleStartedContainer() {
		return assertSingleContainer(actual.containerEvents().started().count());
	}

	@Override
	public TestCaseFailureAssert hasSingleFailedContainer() {
		return assertSingleContainer(actual.containerEvents().failed().count());
	}

	@Override
	public void hasSingleAbortedContainer() {
		Assertions.assertThat(actual.containerEvents().aborted().count()).isEqualTo(1);
	}

	@Override
	public void hasSingleSucceededContainer() {
		Assertions.assertThat(actual.containerEvents().succeeded().count()).isEqualTo(1);
	}

	@Override
	public void hasSingleSkippedContainer() {
		Assertions.assertThat(actual.containerEvents().skipped().count()).isEqualTo(1);
	}

	@Override
	public TestCaseStartedAssert hasSingleDynamicallyRegisteredContainer() {
		return assertSingleContainer(actual.containerEvents().dynamicallyRegistered().count());
	}

	private TestCaseAssertBase assertSingleContainer(long numberOfContainersWithOutcome) {
		Assertions.assertThat(numberOfContainersWithOutcome).isEqualTo(1);
		return new TestCaseAssertBase(actual.containerEvents());
	}

	@Override
	public TestSuiteAssert hasNumberOfStartedTests(int expected) {
		Assertions.assertThat(actual.testEvents().started().count()).isEqualTo(expected);
		return this;
	}

	@Override
	public TestSuiteAssert hasNumberOfFailedTests(int expected) {
		Assertions.assertThat(actual.testEvents().failed().count()).isEqualTo(expected);
		return this;
	}

	@Override
	public TestSuiteAssert hasNumberOfAbortedTests(int expected) {
		Assertions.assertThat(actual.testEvents().aborted().count()).isEqualTo(expected);
		return this;
	}

	@Override
	public TestSuiteAssert hasNumberOfSucceededTests(int expected) {
		Assertions.assertThat(actual.testEvents().succeeded().count()).isEqualTo(expected);
		return this;
	}

	@Override
	public TestSuiteAssert hasNumberOfSkippedTests(int expected) {
		Assertions.assertThat(actual.testEvents().skipped().count()).isEqualTo(expected);
		return this;
	}

	@Override
	public TestSuiteAssert hasNumberOfDynamicallyRegisteredTests(int expected) {
		Assertions.assertThat(actual.testEvents().dynamicallyRegistered().count()).isEqualTo(expected);
		return this;
	}

	@Override
	public TestSuiteAssert hasNumberOfStartedContainers(int expected) {
		Assertions.assertThat(actual.containerEvents().started().count()).isEqualTo(expected);
		return this;
	}

	@Override
	public TestSuiteAssert hasNumberOfFailedContainers(int expected) {
		Assertions.assertThat(actual.containerEvents().failed().count()).isEqualTo(expected);
		return this;
	}

	@Override
	public TestSuiteAssert hasNumberOfAbortedContainers(int expected) {
		Assertions.assertThat(actual.containerEvents().aborted().count()).isEqualTo(expected);
		return this;
	}

	@Override
	public TestSuiteAssert hasNumberOfSucceededContainers(int expected) {
		Assertions.assertThat(actual.containerEvents().succeeded().count()).isEqualTo(expected);
		return this;
	}

	@Override
	public TestSuiteAssert hasNumberOfSkippedContainers(int expected) {
		Assertions.assertThat(actual.containerEvents().skipped().count()).isEqualTo(expected);
		return this;
	}

	@Override
	public TestSuiteAssert hasNumberOfDynamicallyRegisteredContainers(int expected) {
		Assertions.assertThat(actual.containerEvents().dynamicallyRegistered().count()).isEqualTo(expected);
		return this;
	}

	@Override
	public TestSuiteFailureAssert hasFailingTests() {
		Assertions.assertThat(actual.testEvents().failed().count()).isGreaterThan(0);
		return new TestSuiteAssertBase(actual.testEvents());
	}

	@Override
	public TestSuiteFailureAssert hasFailingContainers() {
		Assertions.assertThat(actual.containerEvents().failed().count()).isGreaterThan(0);
		return new TestSuiteAssertBase(actual.containerEvents());
	}

	private List<Map<String, String>> reportEntries() {
		return actual
				.allEvents()
				.reportingEntryPublished()
				.stream()
				.map(event -> event.getPayload(ReportEntry.class))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(ReportEntry::getKeyValuePairs)
				.collect(toList());
	}

	@Override
	public TestSuiteFailureMessageAssert withExceptionInstancesOf(Class<? extends Throwable>... exceptionTypes) {
		return null;
	}

	@Override
	public TestSuiteFailureMessageAssert withExceptions() {
		return null;
	}
}
