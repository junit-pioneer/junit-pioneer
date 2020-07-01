/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.parallel.Execution;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

class RetryingTestTests {

	@Test
	void invalidConfigurationWithTest() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCase.class, "invalidConfigurationWithTest");

		assertThat(results.numberOfDynamicRegisteredTests()).isEqualTo(1);
		assertThat(results.numberOfStartedTests()).isEqualTo(2);
		assertThat(results.numberOfSucceededTests()).isEqualTo(2);
	}

	@Test
	void executedOneEvenWithTwoTestTemplatesTest() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCase.class, "executedOneEvenWithTwoTestTemplates");

		assertThat(results.numberOfDynamicRegisteredTests()).isEqualTo(1);
		assertThat(results.numberOfStartedTests()).isEqualTo(1);
		assertThat(results.numberOfSucceededTests()).isEqualTo(1);
	}

	@Test
	void failsNever_executedOnce_passes() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(RetryingTestTestCase.class, "failsNever");

		assertThat(results.numberOfDynamicRegisteredTests()).isEqualTo(1);
		assertThat(results.numberOfSucceededTests()).isEqualTo(1);
	}

	@Test
	void failsOnlyOnFirstInvocation_executedTwice_passes() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCase.class, "failsOnlyOnFirstInvocation");

		assertThat(results.numberOfDynamicRegisteredTests()).isEqualTo(2);
		assertThat(results.numberOfAbortedTests()).isEqualTo(1);
		assertThat(results.numberOfSucceededTests()).isEqualTo(1);
	}

	@Test
	void failsAlways_executedThreeTimes_fails() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(RetryingTestTestCase.class, "failsAlways");

		assertThat(results.numberOfDynamicRegisteredTests()).isEqualTo(3);
		assertThat(results.numberOfAbortedTests()).isEqualTo(2);
		assertThat(results.numberOfFailedTests()).isEqualTo(1);
	}

	@Test
	void skipByAssumption_executedOnce_skipped() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(RetryingTestTestCase.class, "skipByAssumption");

		assertThat(results.numberOfDynamicRegisteredTests()).isEqualTo(1);
		assertThat(results.numberOfAbortedTests()).isEqualTo(1);
	}

	// TEST CASES -------------------------------------------------------------------

	static class RetryingTestTestCase {

		private static int FAILS_ONLY_ON_FIRST_INVOCATION;

		@Test
		@RetryingTest(3)
		void invalidConfigurationWithTest() {
		}

		@DummyTestTemplate
		@RetryingTest(3)
		void executedOneEvenWithTwoTestTemplates() {
		}

		@RetryingTest(3)
		void failsNever() {
		}

		@RetryingTest(3)
		void failsOnlyOnFirstInvocation() {
			FAILS_ONLY_ON_FIRST_INVOCATION++;
			if (FAILS_ONLY_ON_FIRST_INVOCATION == 1) {
				throw new IllegalArgumentException();
			}
		}

		@RetryingTest(3)
		void failsAlways() {
			throw new IllegalArgumentException();
		}

		@RetryingTest(3)
		void skipByAssumption() {
			Assumptions.assumeFalse(true);
		}

	}

	@Target({ METHOD, ANNOTATION_TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	// the extension is not thread-safe, so it forces execution of all retries
	// onto the same thread
	@Execution(SAME_THREAD)
	@TestTemplate
	public @interface DummyTestTemplate {

	}

}
