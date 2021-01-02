/*
 * Copyright 2016-2021 the original author or authors.
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
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestTemplate;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

class RetryingTestTests {

	@Test
	void invalidConfigurationWithTest() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCase.class, "invalidConfigurationWithTest");

		assertThat(results).hasSingleDynamicallyRegisteredTest();
		assertThat(results).hasNumberOfStartedTests(2).hasNumberOfSucceededTests(2);
	}

	@Test
	void executedOneEvenWithTwoTestTemplatesTest() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCase.class, "executedOneEvenWithTwoTestTemplates");

		assertThat(results).hasSingleDynamicallyRegisteredTest().whichSucceeded();
	}

	@Test
	void failsNever_executedOnce_passes() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(RetryingTestTestCase.class, "failsNever");

		assertThat(results).hasSingleDynamicallyRegisteredTest().whichSucceeded();
	}

	@Test
	void failsOnlyOnFirstInvocation_executedTwice_passes() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCase.class, "failsOnlyOnFirstInvocation");

		assertThat(results)
				.hasNumberOfDynamicallyRegisteredTests(2)
				.hasNumberOfAbortedTests(1)
				.hasNumberOfSucceededTests(1);
	}

	@Test
	void failsAlways_executedThreeTimes_fails() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(RetryingTestTestCase.class, "failsAlways");

		assertThat(results)
				.hasNumberOfDynamicallyRegisteredTests(3)
				.hasNumberOfAbortedTests(2)
				.hasNumberOfFailedTests(1);
	}

	@Test
	void skipByAssumption_executedOnce_skipped() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(RetryingTestTestCase.class, "skipByAssumption");

		assertThat(results).hasSingleDynamicallyRegisteredTest();
		assertThat(results).hasSingleAbortedTest();
	}

	// TEST CASES -------------------------------------------------------------------

	// The test `failsOnlyOnFirstInvocation` needs state to make sure it fails after the first execution.
	// Storing that state in a static field keeps it around from one test suite execution to the next
	// if they are run in the same JVM (as IntelliJ does), which breaks the test.
	// One fix would be a @BeforeAll setup that resets the counter to zero, but for no apparent reason,
	// this lead to flaky tests under threading. Using a `PER_CLASS` lifecycle allows us to make it an
	// instance field and that worked.
	@TestInstance(PER_CLASS)
	static class RetryingTestTestCase {

		private int FAILS_ONLY_ON_FIRST_INVOCATION;

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
	@TestTemplate
	public @interface DummyTestTemplate {

	}

}
