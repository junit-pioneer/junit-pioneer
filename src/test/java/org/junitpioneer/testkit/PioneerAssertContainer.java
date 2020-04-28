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

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.junit.platform.engine.TestExecutionResult;

public class PioneerAssertContainer {

	public static class PioneerAssert extends AbstractAssert<PioneerAssert, ExecutionResults>
			implements SingleReportEntryAssert, FailureAssert, ExceptionAssert {

		public PioneerAssert(ExecutionResults actual) {
			super(actual, PioneerAssert.class);
		}

		public static PioneerAssert assertThat(ExecutionResults actual) {
			return new PioneerAssert(actual);
		}

		public SingleReportEntryAssert hasSingleReportEntry() {
			isNotNull();

			List<Map<String, String>> reportEntries = actual.reportEntries();
			Assertions.assertThat(reportEntries).hasSize(1);
			Map<String, String> reportEntry = reportEntries.get(0);
			Assertions.assertThat(reportEntry).hasSize(1);

			return this;
		}

		public FailureAssert hasSingleFailedTest() {
			isNotNull();
			Assertions.assertThat(actual.numberOfFailedTests()).isEqualTo(1);

			return this;
		}

		public FailureAssert hasSingleFailedContainer() {
			isNotNull();
			Assertions.assertThat(actual.numberOfFailedContainers()).isEqualTo(1);

			return this;
		}

		@Override
		public void withKeyAndValue(String key, String value) {
			Assertions
					.assertThat(actual.reportEntries().get(0).entrySet().iterator().next())
					.isEqualTo(new AbstractMap.SimpleEntry<>(key, value));
		}

		/**
		 * This method should only be called from methods belonging to {@code FailureAssert}.
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

	/**
	 * Used to assert a single report entry.
	 */
	public interface SingleReportEntryAssert {

		void withKeyAndValue(String key, String value);

	}

	/**
	 * Used to assert failed containers/tests.
	 */
	public interface FailureAssert {

		ExceptionAssert andHasException(Class<? extends Throwable> exceptionType);

		ExceptionAssert andHasException();

	}

	/**
	 * Used to assert exceptions thrown by failed tests/containers.
	 */
	public interface ExceptionAssert {

		void withMessageContaining(String... values);

	}

}
