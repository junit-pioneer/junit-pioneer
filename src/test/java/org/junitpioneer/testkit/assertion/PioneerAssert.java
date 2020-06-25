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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.junitpioneer.testkit.ExecutionResults;

public class PioneerAssert extends AbstractAssert<PioneerAssert, ExecutionResults> implements ExecutionResultAssert {

	public static ExecutionResultAssert assertThat(ExecutionResults actual) {
		return new PioneerAssert(actual);
	}

	private PioneerAssert(ExecutionResults actual) {
		super(actual, PioneerAssert.class);
	}

	// This is different from the test/container methods because we can immediately assert that
	// the results contain the expected number of report entries (but can't do the same for
	// tests because we assert the outcomes which in this step is not yet specified).
	@Override
	public ReportEntryAssert hasNumberOfReportEntries(int expected) {
		List<Map<String, String>> entries = actual.reportEntries();
		Assertions.assertThat(entries).hasSize(expected);
		Integer[] ones = IntStream.generate(() -> 1).limit(expected).boxed().toArray(Integer[]::new);
		Assertions.assertThat(entries).extracting(Map::size).containsExactly(ones);

		List<Map.Entry<String, String>> entryList = actual
				.reportEntries()
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
		Assertions.assertThat(actual.reportEntries()).isEmpty();
	}

	@Override
	public TestAssert hasNumberOfTests(int expected) {
		return new TestAssertBase(actual.testEvents(), expected);
	}

	@Override
	public TestAssert hasSingleTest() {
		return hasNumberOfTests(1);
	}

	@Override
	public TestAssert hasNoTests() {
		return hasNumberOfTests(0);
	}

	@Override
	public TestAssert hasNumberOfContainers(int expected) {
		return new TestAssertBase(actual.containerEvents(), expected);
	}

	@Override
	public TestAssert hasSingleContainer() {
		return hasNumberOfContainers(1);
	}

	@Override
	public TestAssert hasNoContainers() {
		return hasNumberOfContainers(0);
	}

}
