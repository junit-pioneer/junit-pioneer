/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junitpioneer.testkit.PioneerTestKit.abort;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.TestTemplate;
import org.junit.platform.testkit.engine.Execution;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

class RetryingTestExtensionTests {

	private static final int SUSPEND_FOR = 10;

	@Test
	void invalidConfigurationWithTest() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCases.class, "invalidConfigurationWithTest");

		assertThat(results).hasSingleDynamicallyRegisteredTest();
		assertThat(results).hasNumberOfStartedTests(2).hasNumberOfSucceededTests(2);
	}

	@Test
	void executedOneEvenWithTwoTestTemplatesTest() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCases.class, "executedOneEvenWithTwoTestTemplates");

		assertThat(results).hasSingleDynamicallyRegisteredTest().whichSucceeded();
	}

	@Test
	void failsNever_executedOnce_passes() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(RetryingTestTestCases.class, "failsNever");

		assertThat(results).hasSingleDynamicallyRegisteredTest().whichSucceeded();
	}

	@Test
	void failsOnlyOnFirstInvocation_executedTwice_passes() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCases.class, "failsOnlyOnFirstInvocation");

		assertThat(results)
				.hasNumberOfDynamicallyRegisteredTests(2)
				.hasNumberOfAbortedTests(1)
				.hasNumberOfSucceededTests(1);
	}

	@Test
	void hasAName_executedTwice_passes_publishesCustomName() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethodWithParameterTypes(RetryingTestTestCases.class, "hasAName", TestInfo.class,
					TestReporter.class);

		assertThat(results)
				.hasNumberOfDynamicallyRegisteredTests(2)
				.hasNumberOfAbortedTests(1)
				.hasNumberOfSucceededTests(1);
		assertThat(results)
				.hasNumberOfReportEntries(2)
				.withValues("[1] retrying test invocation with custom name",
					"[2] retrying test invocation with custom name");
	}

	@Test
	void hasNoName_fails() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(RetryingTestTestCases.class, "hasNoName");

		assertThat(results).hasNumberOfDynamicallyRegisteredTests(0);
	}

	@Test
	void failsOnlyOnFirstInvocationWithExpectedException_executedTwice_passes() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCases.class, "failsOnlyOnFirstInvocationWithExpectedException");

		assertThat(results)
				.hasNumberOfDynamicallyRegisteredTests(2)
				.hasNumberOfAbortedTests(1)
				.hasNumberOfSucceededTests(1);
	}

	@Test
	void failsOnlyOnFirstInvocationWithUnexpectedException_executedOnce_fails() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCases.class, "failsOnlyOnFirstInvocationWithUnexpectedException");

		assertThat(results).hasNumberOfDynamicallyRegisteredTests(1).hasNumberOfFailedTests(1);
	}

	@Test
	void failsOnlyOnFirstInvocationWithUnexpectedException_executedTwice_fails() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCases.class, "failsFirstWithExpectedThenWithUnexpectedException");

		assertThat(results)
				.hasNumberOfDynamicallyRegisteredTests(2)
				.hasNumberOfAbortedTests(1)
				.hasNumberOfFailedTests(1);
	}

	@Test
	void failsAlways_executedThreeTimes_fails() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(RetryingTestTestCases.class, "failsAlways");

		assertThat(results)
				.hasNumberOfDynamicallyRegisteredTests(3)
				.hasNumberOfAbortedTests(2)
				.hasNumberOfFailedTests(1);
	}

	@Test
	void skipByAssumption_executedOnce_skipped() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(RetryingTestTestCases.class, "skipByAssumption");

		assertThat(results).hasSingleDynamicallyRegisteredTest();
		assertThat(results).hasSingleAbortedTest();
	}

	@Test
	void failsFirstWithExpectedExceptionThenSkippedByAssumption_executedTwice_skipped() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCases.class,
					"failsFirstWithExpectedExceptionThenSkippedByAssumption");

		assertThat(results).hasNumberOfDynamicallyRegisteredTests(2).hasNumberOfAbortedTests(2);
	}

	@Test
	void executesTwice_succeeds() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(RetryingTestTestCases.class, "executesTwice");

		assertThat(results)
				.hasNumberOfDynamicallyRegisteredTests(2)
				.hasNumberOfAbortedTests(0)
				.hasNumberOfFailedTests(0)
				.hasNumberOfSucceededTests(2);
	}

	@Test
	void executesTwiceWithTwoFails_succeeds() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCases.class, "executesTwiceWithTwoFails");

		assertThat(results)
				.hasNumberOfDynamicallyRegisteredTests(4)
				.hasNumberOfAbortedTests(2)
				.hasNumberOfFailedTests(0)
				.hasNumberOfSucceededTests(2);
	}

	@Test
	void executesOnceWithThreeFails_fails() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCases.class, "executesOnceWithThreeFails");

		assertThat(results)
				.hasNumberOfDynamicallyRegisteredTests(4)
				.hasNumberOfAbortedTests(2)
				.hasNumberOfFailedTests(1)
				.hasNumberOfSucceededTests(1);
	}

	@Test
	void failsThreeTimes_fails() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(RetryingTestTestCases.class, "failsThreeTimes");

		assertThat(results)
				.hasNumberOfDynamicallyRegisteredTests(3)
				.hasNumberOfAbortedTests(2)
				.hasNumberOfFailedTests(1)
				.hasNumberOfSucceededTests(0);
	}

	@Test
	void missingMaxAttempts_fails() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(RetryingTestTestCases.class, "missingMaxAttempts");

		assertThat(results).hasNumberOfDynamicallyRegisteredTests(0);
	}

	@Test
	void valueLessThanOne_fails() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(RetryingTestTestCases.class, "valueLessThanOne");

		assertThat(results).hasNumberOfDynamicallyRegisteredTests(0);
	}

	@Test
	void maxAttemptsLessThanOne_fails() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCases.class, "maxAttemptsLessThanOne");

		assertThat(results).hasNumberOfDynamicallyRegisteredTests(0);
	}

	@Test
	void maxAttemptsAndValueSet_fails() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCases.class, "maxAttemptsAndValueSet");

		assertThat(results).hasNumberOfDynamicallyRegisteredTests(0);
	}

	@Test
	void maxAttemptsEqualsMinSuccess_fails() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCases.class, "maxAttemptsEqualsMinSuccess");

		assertThat(results).hasNumberOfDynamicallyRegisteredTests(0);
	}

	@Test
	void maxAttemptsEqualsOne_fails() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCases.class, "maxAttemptsEqualsOne");

		assertThat(results).hasNumberOfDynamicallyRegisteredTests(0);
	}

	@Test
	void maxAttemptsLessThanMinSuccess_fails() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCases.class, "maxAttemptsLessThanMinSuccess");

		assertThat(results).hasNumberOfDynamicallyRegisteredTests(0);
	}

	@Test
	void minSuccessLessThanOne_fails() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCases.class, "minSuccessLessThanOne");

		assertThat(results).hasNumberOfDynamicallyRegisteredTests(0);
	}

	@Test
	void suspendForLessThanZero_fails() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCases.class, "suspendForLessThanZero");

		assertThat(results).hasNumberOfDynamicallyRegisteredTests(0);
	}

	@Test
	void failThreeTimesWithSuspend() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCases.class, "failThreeTimesWithSuspend");

		assertThat(results)
				.hasNumberOfDynamicallyRegisteredTests(3)
				.hasNumberOfAbortedTests(2)
				.hasNumberOfFailedTests(1)
				.hasNumberOfSucceededTests(0);

		assertSuspendedFor(results, SUSPEND_FOR);
	}

	@Test
	void failThreeTimesWithoutSuspend() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCases.class, "failThreeTimesWithoutSuspend");

		assertThat(results)
				.hasNumberOfDynamicallyRegisteredTests(3)
				.hasNumberOfAbortedTests(2)
				.hasNumberOfFailedTests(1)
				.hasNumberOfSucceededTests(0);

		assertSuspendedFor(results, 0);
	}

	private void assertSuspendedFor(ExecutionResults results, long greaterThanOrEqualTo) {
		List<Execution> finishedExecutions = results.testEvents().executions().finished().list();
		List<Execution> startedExecutions = results.testEvents().executions().started().list();

		// Compare 'finished' execution timestamp with follow-up 'started' execution,
		// skipping the 1st 'started' and final 'finished'.
		// This duration should be equals to or greater than 'suspendFor'.
		for (int i = 0; i < finishedExecutions.size() - 1; i++) {
			Execution finished = finishedExecutions.get(i);
			Execution nextStarted = startedExecutions.get(i + 1);

			Instant finishedAt = finished.getEndInstant();
			Instant nextStartedAt = nextStarted.getStartInstant();

			long suspendedFor = Duration.between(finishedAt, nextStartedAt).toMillis();

			Assertions.assertThat(suspendedFor).isGreaterThanOrEqualTo(greaterThanOrEqualTo);
		}
	}

	// TEST CASES -------------------------------------------------------------------

	// Some tests require state to keep track of the number of test executions.
	// Storing that state in a static field keeps it around from one test suite execution to the next
	// if they are run in the same JVM (as IntelliJ does), which breaks the test.
	// One fix would be a @BeforeAll setup that resets the counter to zero, but for no apparent reason,
	// this lead to flaky tests under threading. Using a `PER_CLASS` lifecycle allows us to make it an
	// instance field and that worked.
	@TestInstance(PER_CLASS)
	static class RetryingTestTestCases {

		private int executionCount;

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

		@RetryingTest(value = 3, name = "[{index}] retrying test invocation with custom name")
		void hasAName(TestInfo info, TestReporter reporter) {
			reporter.publishEntry(info.getDisplayName());
			executionCount++;
			if (executionCount == 1) {
				throw new IllegalArgumentException();
			}
		}

		@RetryingTest(value = 3, name = "")
		void hasNoName() {
			// Do nothing
		}

		@RetryingTest(3)
		void failsOnlyOnFirstInvocation() {
			executionCount++;
			if (executionCount == 1) {
				throw new IllegalArgumentException();
			}
		}

		@RetryingTest(value = 3, onExceptions = IllegalArgumentException.class)
		void failsOnlyOnFirstInvocationWithExpectedException() {
			executionCount++;
			if (executionCount == 1) {
				throw new IllegalArgumentException();
			}
		}

		@RetryingTest(value = 3, onExceptions = IllegalArgumentException.class)
		void failsOnlyOnFirstInvocationWithUnexpectedException() {
			executionCount++;
			if (executionCount == 1) {
				throw new NullPointerException();
			}
		}

		@RetryingTest(value = 3, onExceptions = IllegalArgumentException.class)
		void failsFirstWithExpectedThenWithUnexpectedException() {
			executionCount++;
			if (executionCount == 1) {
				throw new IllegalArgumentException();
			}
			if (executionCount == 2) {
				throw new NullPointerException();
			}
		}

		@RetryingTest(3)
		void failsAlways() {
			throw new IllegalArgumentException();
		}

		@RetryingTest(3)
		void skipByAssumption() {
			abort();
		}

		@RetryingTest(value = 3, onExceptions = IllegalArgumentException.class)
		void failsFirstWithExpectedExceptionThenSkippedByAssumption() {
			executionCount++;
			if (executionCount == 1) {
				throw new IllegalArgumentException();
			}
			if (executionCount == 2) {
				abort();
			}
		}

		@RetryingTest(maxAttempts = 4, minSuccess = 2)
		void executesTwice() {
		}

		@RetryingTest(maxAttempts = 4, minSuccess = 2)
		void executesTwiceWithTwoFails() {
			executionCount++;
			if (executionCount == 2 || executionCount == 3) {
				throw new IllegalArgumentException();
			}
		}

		@RetryingTest(maxAttempts = 4, minSuccess = 2)
		void executesOnceWithThreeFails() {
			executionCount++;
			if (executionCount != 2) {
				throw new IllegalArgumentException();
			}
		}

		@RetryingTest(maxAttempts = 4, minSuccess = 2)
		void failsThreeTimes() {
			throw new IllegalArgumentException();
		}

		@RetryingTest
		void missingMaxAttempts() {
			// Do nothing
		}

		@RetryingTest(value = 0)
		void valueLessThanOne() {
			// Do nothing
		}

		@RetryingTest(maxAttempts = 0)
		void maxAttemptsLessThanOne() {
			// Do nothing
		}

		@RetryingTest(value = 1, maxAttempts = 1)
		void maxAttemptsAndValueSet() {
			// Do nothing
		}

		@RetryingTest(maxAttempts = 2, minSuccess = 2)
		void maxAttemptsEqualsMinSuccess() {
			// Do nothing
		}

		@RetryingTest(maxAttempts = 1, minSuccess = 1)
		void maxAttemptsEqualsOne() {
			// Do nothing
		}

		@RetryingTest(maxAttempts = -1, minSuccess = 1)
		void maxAttemptsLessThanMinSuccess() {
			// Do nothing
		}

		@RetryingTest(maxAttempts = 2, minSuccess = 0)
		void minSuccessLessThanOne() {
			// Do nothing
		}

		@RetryingTest(suspendForMs = -1)
		void suspendForLessThanZero() {
			// Do nothing
		}

		@RetryingTest(maxAttempts = 3, suspendForMs = SUSPEND_FOR)
		void failThreeTimesWithSuspend() {
			throw new IllegalArgumentException();
		}

		@RetryingTest(maxAttempts = 3)
		void failThreeTimesWithoutSuspend() {
			throw new IllegalArgumentException();
		}

	}

	@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	@TestTemplate
	public @interface DummyTestTemplate {

	}

}
