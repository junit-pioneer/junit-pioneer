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

class RetryingTestExtensionTests {

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

	@Test
	void executesTwice_succeeds() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(RetryingTestTestCase.class, "executesTwice");

		assertThat(results)
				.hasNumberOfDynamicallyRegisteredTests(2)
				.hasNumberOfAbortedTests(0)
				.hasNumberOfFailedTests(0)
				.hasNumberOfSucceededTests(2);
	}

	@Test
	void executesTwiceWithTwoFails_succeeds() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCase.class, "executesTwiceWithTwoFails");

		assertThat(results)
				.hasNumberOfDynamicallyRegisteredTests(4)
				.hasNumberOfAbortedTests(2)
				.hasNumberOfFailedTests(0)
				.hasNumberOfSucceededTests(2);
	}

	@Test
	void executesOnceWithThreeFails_fails() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCase.class, "executesOnceWithThreeFails");

		assertThat(results)
				.hasNumberOfDynamicallyRegisteredTests(4)
				.hasNumberOfAbortedTests(2)
				.hasNumberOfFailedTests(1)
				.hasNumberOfSucceededTests(1);
	}

	@Test
	void failsThreeTimes_fails() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(RetryingTestTestCase.class, "failsThreeTimes");

		assertThat(results)
				.hasNumberOfDynamicallyRegisteredTests(3)
				.hasNumberOfAbortedTests(2)
				.hasNumberOfFailedTests(1)
				.hasNumberOfSucceededTests(0);
	}

	@Test
	void missingMaxAttempts_fails() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(RetryingTestTestCase.class, "missingMaxAttempts");

		assertThat(results).hasNumberOfDynamicallyRegisteredTests(0);
	}

	@Test
	void valueLessThanOne_fails() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(RetryingTestTestCase.class, "valueLessThanOne");

		assertThat(results).hasNumberOfDynamicallyRegisteredTests(0);
	}

	@Test
	void maxAttemptsLessThanOne_fails() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCase.class, "maxAttemptsLessThanOne");

		assertThat(results).hasNumberOfDynamicallyRegisteredTests(0);
	}

	@Test
	void maxAttemptsAndValueSet_fails() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCase.class, "maxAttemptsAndValueSet");

		assertThat(results).hasNumberOfDynamicallyRegisteredTests(0);
	}

	@Test
	void maxAttemptsEqualsMinSuccess_fails() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCase.class, "maxAttemptsEqualsMinSuccess");

		assertThat(results).hasNumberOfDynamicallyRegisteredTests(0);
	}

	@Test
	void maxAttemptsLessThanMinSuccess_fails() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCase.class, "maxAttemptsLessThanMinSuccess");

		assertThat(results).hasNumberOfDynamicallyRegisteredTests(0);
	}

	@Test
	void minSuccessLessThanOne_fails() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCase.class, "minSuccessLessThanOne");

		assertThat(results).hasNumberOfDynamicallyRegisteredTests(0);
	}

	// TEST CASES -------------------------------------------------------------------

	// Some tests require state to keep track of the number of test executions.
	// Storing that state in a static field keeps it around from one test suite execution to the next
	// if they are run in the same JVM (as IntelliJ does), which breaks the test.
	// One fix would be a @BeforeAll setup that resets the counter to zero, but for no apparent reason,
	// this lead to flaky tests under threading. Using a `PER_CLASS` lifecycle allows us to make it an
	// instance field and that worked.
	@TestInstance(PER_CLASS)
	static class RetryingTestTestCase {

		private int EXECUTION_COUNT;

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
			EXECUTION_COUNT++;
			if (EXECUTION_COUNT == 1) {
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

		@RetryingTest(maxAttempts = 4, minSuccess = 2)
		void executesTwice() {
		}

		@RetryingTest(maxAttempts = 4, minSuccess = 2)
		void executesTwiceWithTwoFails() {
			EXECUTION_COUNT++;
			if (EXECUTION_COUNT == 2 || EXECUTION_COUNT == 3) {
				throw new IllegalArgumentException();
			}
		}

		@RetryingTest(maxAttempts = 4, minSuccess = 2)
		void executesOnceWithThreeFails() {
			EXECUTION_COUNT++;
			if (EXECUTION_COUNT != 2) {
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

		@RetryingTest(maxAttempts = 1, minSuccess = 1)
		void maxAttemptsEqualsMinSuccess() {
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

	}

	@Target({ METHOD, ANNOTATION_TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	@TestTemplate
	public @interface DummyTestTemplate {

	}

}
