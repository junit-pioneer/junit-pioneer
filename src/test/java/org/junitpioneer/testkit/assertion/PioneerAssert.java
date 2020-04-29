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

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.junit.platform.engine.TestExecutionResult;
import org.junitpioneer.testkit.ExecutionResults;

public class PioneerAssert extends AbstractAssert<PioneerAssert, ExecutionResults>
		implements SingleReportEntryAssert, FailureAssert, ExceptionAssert, ExecutionResultAssert {

	private PioneerAssert(ExecutionResults actual) {
		super(actual, PioneerAssert.class);
	}

	public static ExecutionResultAssert assertThat(ExecutionResults actual) {
		return new PioneerAssert(actual);
	}

	@Override
	public SingleReportEntryAssert hasSingleReportEntry() {
		isNotNull();

		List<Map<String, String>> reportEntries = actual.reportEntries();
		Assertions.assertThat(reportEntries).hasSize(1);
		Map<String, String> reportEntry = reportEntries.get(0);
		Assertions.assertThat(reportEntry).hasSize(1);

		return this;
	}

	@Override
	public FailureAssert hasSingleFailedTest() {
		return hasNumberOfFailedTests(1);
	}

	@Override
	public FailureAssert hasSingleFailedContainer() {
		return hasNumberOfFailedContainers(1);
	}

	@Override
	public FailureAssert hasNumberOfFailedTests(long expected) {
		isNotNull();
		Assertions.assertThat(actual.numberOfFailedTests()).isEqualTo(expected);

		return this;
	}

	@Override
	public FailureAssert hasNumberOfFailedContainers(long expected) {
		isNotNull();
		Assertions.assertThat(actual.numberOfFailedContainers()).isEqualTo(expected);

		return this;
	}

	@Override
	public void hasSingleStartedTest() {
		hasNumberOfStartedTests(1);
	}

	@Override
	public void hasNumberOfStartedTests(long expected) {
		Assertions.assertThat(actual.numberOfStartedTests()).isEqualTo(expected);
	}

	@Override
	public void withKeyAndValue(String key, String value) {
		Assertions
				.assertThat(actual.reportEntries().get(0).entrySet().iterator().next())
				.isEqualTo(new AbstractMap.SimpleEntry<>(key, value));
	}

	/**
	 * This method should only be called from methods belonging to {@code FailureAssert} or after.
	 */
	private Optional<Throwable> firstFailureThrowable() {
		return actual
				.allEvents()
				.failed()
				.stream()
				.findFirst()
				.flatMap(first -> first.getPayload(TestExecutionResult.class))
				.flatMap(TestExecutionResult::getThrowable);

	}

	@Override
	public ExceptionAssert andHasException(Class<? extends Throwable> exceptionType) {
		Optional<Throwable> exception = firstFailureThrowable();

		Assertions.assertThat(exception).isPresent();
		Assertions.assertThat(exception.get()).isInstanceOf(exceptionType);

		return this;
	}

	@Override
	public ExceptionAssert andHasException() {
		Assertions.assertThat(firstFailureThrowable()).isPresent();

		return this;
	}

	@Override
	public void withMessageContaining(String... values) {
		Assertions.assertThat(firstFailureThrowable().get()).hasMessageContainingAll(values);
	}

}
