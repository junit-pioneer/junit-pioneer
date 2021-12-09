/*
 * Copyright 2016-2021 the original author or authors.
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
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.testkit.engine.Events;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.assertion.reportentry.ReportEntryValueAssert;
import org.junitpioneer.testkit.assertion.single.TestCaseFailureAssert;
import org.junitpioneer.testkit.assertion.single.TestCaseStartedAssert;
import org.junitpioneer.testkit.assertion.suite.TestSuiteAssert;
import org.junitpioneer.testkit.assertion.suite.TestSuiteContainersAssert;
import org.junitpioneer.testkit.assertion.suite.TestSuiteTestsAssert;

public class PioneerAssert extends AbstractAssert<PioneerAssert, ExecutionResults>
		implements ExecutionResultAssert, TestSuiteAssert, TestSuiteTestsAssert.TestSuiteTestsFailureAssert,
		TestSuiteContainersAssert.TestSuiteContainersFailureAssert {

	private boolean test = true;

	public static ExecutionResultAssert assertThat(ExecutionResults actual) {
		return new PioneerAssert(actual);
	}

	private PioneerAssert(ExecutionResults actual) {
		super(actual, PioneerAssert.class);
	}

	@Override
	public ReportEntryValueAssert hasNumberOfReportEntries(int expected) {
		try {
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
		catch (AssertionError error) {
			getAllExceptions(actual.allEvents().reportingEntryPublished()).forEach(error::addSuppressed);
			throw error;
		}
	}

	@Override
	public ReportEntryValueAssert hasSingleReportEntry() {
		return hasNumberOfReportEntries(1);
	}

	@Override
	public void hasNoReportEntries() {
		try {
			Assertions.assertThat(reportEntries()).isEmpty();
		}
		catch (AssertionError error) {
			getAllExceptions(actual.allEvents()).forEach(error::addSuppressed);
			throw error;
		}
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
		assertSingleTest(actual.testEvents().aborted().count());
	}

	@Override
	public void hasSingleSucceededTest() {
		assertSingleTest(actual.testEvents().succeeded().count());
	}

	@Override
	public void hasSingleSkippedTest() {
		assertSingleTest(actual.testEvents().skipped().count());
	}

	@Override
	public TestCaseStartedAssert hasSingleDynamicallyRegisteredTest() {
		return assertSingleTest(actual.testEvents().dynamicallyRegistered().count());
	}

	private TestCaseAssertBase assertSingleTest(long numberOfTestsWithOutcome) {
		try {
			Assertions.assertThat(numberOfTestsWithOutcome).isEqualTo(1);
			return new TestCaseAssertBase(actual.testEvents());
		}
		catch (AssertionError error) {
			getAllExceptions(actual.allEvents()).forEach(error::addSuppressed);
			throw error;
		}
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
		assertSingleContainer(actual.containerEvents().aborted().count());
	}

	@Override
	public void hasSingleSucceededContainer() {
		assertSingleContainer(actual.containerEvents().succeeded().count());
	}

	@Override
	public void hasSingleSkippedContainer() {
		assertSingleContainer(actual.containerEvents().skipped().count());
	}

	@Override
	public TestCaseStartedAssert hasSingleDynamicallyRegisteredContainer() {
		return assertSingleContainer(actual.containerEvents().dynamicallyRegistered().count());
	}

	private TestCaseAssertBase assertSingleContainer(long numberOfContainersWithOutcome) {
		try {
			Assertions.assertThat(numberOfContainersWithOutcome).isEqualTo(1);
			return new TestCaseAssertBase(actual.containerEvents());
		}
		catch (AssertionError error) {
			getAllExceptions(actual.allEvents()).forEach(error::addSuppressed);
			throw error;
		}
	}

	@Override
	public TestSuiteTestsAssert hasNumberOfStartedTests(int expected) {
		return hasNumberOfSpecificTests(actual.testEvents().started().count(), expected);
	}

	@Override
	public TestSuiteTestsFailureAssert hasNumberOfFailedTests(int expected) {
		this.test = true;
		return hasNumberOfSpecificTests(actual.testEvents().failed().count(), expected);
	}

	@Override
	public TestSuiteTestsAssert hasNumberOfAbortedTests(int expected) {
		return hasNumberOfSpecificTests(actual.testEvents().aborted().count(), expected);
	}

	@Override
	public TestSuiteTestsAssert hasNumberOfSucceededTests(int expected) {
		return hasNumberOfSpecificTests(actual.testEvents().succeeded().count(), expected);
	}

	@Override
	public TestSuiteTestsAssert hasNumberOfSkippedTests(int expected) {
		return hasNumberOfSpecificTests(actual.testEvents().skipped().count(), expected);
	}

	@Override
	public TestSuiteTestsAssert hasNumberOfDynamicallyRegisteredTests(int expected) {
		return hasNumberOfSpecificTests(actual.testEvents().dynamicallyRegistered().count(), expected);
	}

	private TestSuiteTestsFailureAssert hasNumberOfSpecificTests(long tests, int expected) {
		try {
			Assertions.assertThat(tests).isEqualTo(expected);
			return this;
		}
		catch (AssertionError error) {
			getAllExceptions(actual.allEvents()).forEach(error::addSuppressed);
			throw error;
		}
	}

	@Override
	public TestSuiteContainersAssert hasNumberOfStartedContainers(int expected) {
		return hasNumberOfSpecificContainers(actual.containerEvents().started().count(), expected);
	}

	@Override
	public TestSuiteContainersFailureAssert hasNumberOfFailedContainers(int expected) {
		this.test = false;
		return hasNumberOfSpecificContainers(actual.containerEvents().failed().count(), expected);
	}

	@Override
	public TestSuiteContainersAssert hasNumberOfAbortedContainers(int expected) {
		return hasNumberOfSpecificContainers(actual.containerEvents().aborted().count(), expected);
	}

	@Override
	public TestSuiteContainersAssert hasNumberOfSucceededContainers(int expected) {
		return hasNumberOfSpecificContainers(actual.containerEvents().succeeded().count(), expected);
	}

	@Override
	public TestSuiteContainersAssert hasNumberOfSkippedContainers(int expected) {
		return hasNumberOfSpecificContainers(actual.containerEvents().skipped().count(), expected);
	}

	@Override
	public TestSuiteContainersAssert hasNumberOfDynamicallyRegisteredContainers(int expected) {
		return hasNumberOfSpecificContainers(actual.containerEvents().dynamicallyRegistered().count(), expected);
	}

	private TestSuiteContainersFailureAssert hasNumberOfSpecificContainers(long containers, int expected) {
		try {
			Assertions.assertThat(containers).isEqualTo(expected);
			return this;
		}
		catch (AssertionError error) {
			getAllExceptions(actual.allEvents()).forEach(error::addSuppressed);
			throw error;
		}
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

	@SafeVarargs
	@Override
	public final ListAssert<String> withExceptionInstancesOf(Class<? extends Throwable>... exceptionTypes) {
		return assertExceptions(events -> {
			Stream<Class<? extends Throwable>> classStream = getAllExceptions(events).map(Throwable::getClass);
			Assertions.assertThat(classStream).containsOnly(exceptionTypes);
		});
	}

	@Override
	public ListAssert<String> withExceptions() {
		return assertExceptions(
			events -> Assertions.assertThat(events.failed().count()).isEqualTo(getAllExceptions(events).count()));
	}

	private ListAssert<String> assertExceptions(Consumer<Events> assertion) {
		try {
			Events events = getProperEvents();
			assertion.accept(events);
			return new ListAssert<>(getAllExceptions(getProperEvents()).map(Throwable::getMessage));
		}
		catch (AssertionError error) {
			getAllExceptions(actual.allEvents()).forEach(error::addSuppressed);
			throw error;
		}
	}

	private Events getProperEvents() {
		return this.test ? actual.testEvents() : actual.containerEvents();
	}

	static Stream<Throwable> getAllExceptions(Events events) {
		return events
				.stream()
				.map(fail -> fail.getPayload(TestExecutionResult.class))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(TestExecutionResult::getThrowable)
				.filter(Optional::isPresent)
				.map(Optional::get);
	}

	@Override
	public void assertingExceptions(Predicate<List<Throwable>> predicate) {
		List<Throwable> thrownExceptions = getAllExceptions(getProperEvents()).collect(toList());
		Assertions.assertThat(predicate).accepts(thrownExceptions);
	}

	@Override
	public void andThenCheckExceptions(Consumer<List<Throwable>> testFunction) {
		List<Throwable> thrownExceptions = getAllExceptions(getProperEvents()).collect(toList());
		testFunction.accept(thrownExceptions);
	}

}
