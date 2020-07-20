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

public class PioneerAssert extends AbstractAssert<PioneerAssert, ExecutionResults> implements ExecutionResultAssert {

	public static ExecutionResultAssert assertThat(ExecutionResults actual) {
		return new PioneerAssert(actual);
	}

	private PioneerAssert(ExecutionResults actual) {
		super(actual, PioneerAssert.class);
	}

	@Override
	public ReportEntryAssert hasNumberOfReportEntries(int expected) {
		List<Map<String, String>> entries = reportEntries();
		Assertions.assertThat(entries).hasSize(expected);
		Integer[] ones = IntStream.generate(() -> 1).limit(expected).boxed().toArray(Integer[]::new);
		Assertions.assertThat(entries).extracting(Map::size).containsExactly(ones);

		List<Map.Entry<String, String>> entryList = entries
				.stream()
				.flatMap(map -> map.entrySet().stream())
				.collect(Collectors.toList());

		return new ReportEntryAssert(entryList, expected);
	}

	@Override
	public ReportEntryAssert hasSingleReportEntry() {
		return hasNumberOfReportEntries(1);
	}

	@Override
	public void hasNoReportEntries() {
		Assertions.assertThat(reportEntries()).isEmpty();
	}

	@Override
	public TestCaseAssert hasSingleStartedTest() {
		return assertSingleTest(actual.testEvents().started().count());
	}

	@Override
	public FailureAssert hasSingleFailedTest() {
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
	public TestCaseAssert hasSingleDynamicallyRegisteredTest() {
		return assertSingleTest(actual.testEvents().dynamicallyRegistered().count());
	}

	private TestAssertBase assertSingleTest(long numberOfTestsWithOutcome) {
		Assertions.assertThat(numberOfTestsWithOutcome).isEqualTo(1);
		return new TestAssertBase(actual.testEvents());
	}

	@Override
	public TestCaseAssert hasSingleStartedContainer() {
		return assertSingleContainer(actual.containerEvents().started().count());
	}

	@Override
	public FailureAssert hasSingleFailedContainer() {
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
	public TestCaseAssert hasSingleDynamicallyRegisteredContainer() {
		return assertSingleContainer(actual.containerEvents().dynamicallyRegistered().count());
	}

	private TestAssertBase assertSingleContainer(long numberOfContainersWithOutcome) {
		Assertions.assertThat(numberOfContainersWithOutcome).isEqualTo(1);
		return new TestAssertBase(actual.containerEvents());
	}

	@Override
	public ExecutionResultAssert hasNumberOfStartedTests(int expected) {
		Assertions.assertThat(actual.testEvents().started().count()).isEqualTo(expected);
		return this;
	}

	@Override
	public ExecutionResultAssert hasNumberOfFailedTests(int expected) {
		Assertions.assertThat(actual.testEvents().failed().count()).isEqualTo(expected);
		return this;
	}

	@Override
	public ExecutionResultAssert hasNumberOfAbortedTests(int expected) {
		Assertions.assertThat(actual.testEvents().aborted().count()).isEqualTo(expected);
		return this;
	}

	@Override
	public ExecutionResultAssert hasNumberOfSucceededTests(int expected) {
		Assertions.assertThat(actual.testEvents().succeeded().count()).isEqualTo(expected);
		return this;
	}

	@Override
	public ExecutionResultAssert hasNumberOfSkippedTests(int expected) {
		Assertions.assertThat(actual.testEvents().skipped().count()).isEqualTo(expected);
		return this;
	}

	@Override
	public ExecutionResultAssert hasNumberOfDynamicallyRegisteredTests(int expected) {
		Assertions.assertThat(actual.testEvents().dynamicallyRegistered().count()).isEqualTo(expected);
		return this;
	}

	@Override
	public ExecutionResultAssert hasNumberOfStartedContainers(int expected) {
		Assertions.assertThat(actual.containerEvents().started().count()).isEqualTo(expected);
		return this;
	}

	@Override
	public ExecutionResultAssert hasNumberOfFailedContainers(int expected) {
		Assertions.assertThat(actual.containerEvents().failed().count()).isEqualTo(expected);
		return this;
	}

	@Override
	public ExecutionResultAssert hasNumberOfAbortedContainers(int expected) {
		Assertions.assertThat(actual.containerEvents().aborted().count()).isEqualTo(expected);
		return this;
	}

	@Override
	public ExecutionResultAssert hasNumberOfSucceededContainers(int expected) {
		Assertions.assertThat(actual.containerEvents().succeeded().count()).isEqualTo(expected);
		return this;
	}

	@Override
	public ExecutionResultAssert hasNumberOfSkippedContainers(int expected) {
		Assertions.assertThat(actual.containerEvents().skipped().count()).isEqualTo(expected);
		return this;
	}

	@Override
	public ExecutionResultAssert hasNumberOfDynamicallyRegisteredContainers(int expected) {
		Assertions.assertThat(actual.containerEvents().dynamicallyRegistered().count()).isEqualTo(expected);
		return this;
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

}
