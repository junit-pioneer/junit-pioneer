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
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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

		assertThat(results).hasSingleTest().dynamicallyRegistered();
		assertThat(results).hasNumberOfTests(2).thatStarted().andAllOfThemSucceeded();
	}

	@Test
	void executedOneEvenWithTwoTestTemplatesTest() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCase.class, "executedOneEvenWithTwoTestTemplates");

		assertThat(results).hasSingleTest().dynamicallyRegistered().thatStarted().andAllOfThemSucceeded();
	}

	@Test
	void failsNever_executedOnce_passes() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(RetryingTestTestCase.class, "failsNever");

		assertThat(results).hasSingleTest().dynamicallyRegistered().thatSucceeded();
	}

	@Test
	void failsOnlyOnFirstInvocation_executedTwice_passes() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RetryingTestTestCase.class, "failsOnlyOnFirstInvocation");

		assertThat(results)
				.hasNumberOfTests(2)
				.dynamicallyRegistered()
				.thatStarted()
				.andThisManyAborted(1)
				.theRestSucceeded();
	}

	@Test
	void failsAlways_executedThreeTimes_fails() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(RetryingTestTestCase.class, "failsAlways");

		assertThat(results)
				.hasNumberOfTests(3)
				.dynamicallyRegistered()
				.thatStarted()
				.andThisManyAborted(2)
				.theRestSucceeded();
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
