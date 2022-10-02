/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

public class RetryingTestExtensionDemo {

	// tag::retrying_failsNever[]
	@RetryingTest(3)
	void failsNever() {
		// passing test code
	}
	// end::retrying_failsNever[]

	// tag::retrying_fails_on_first_but_passes_on_second[]
	@RetryingTest(3)
	void failsOnlyOnFirstInvocation() {
		// test code that fails on first execution
		// but passes on the second
	}
	// end::retrying_fails_on_first_but_passes_on_second[]

	// tag::retrying_fails_always[]
	@RetryingTest(3)
	void failsAlways() {
		// test code that always fails
	}
	// end::retrying_fails_always[]

	// tag::retrying_aborted[]
	@RetryingTest(3)
	void aborted() {
		// test code that is aborted,
		// e.g. because of an `Assumption`.
	}
	// end::retrying_aborted[]

	// tag::retrying_configure_numbers_of_success[]
	@RetryingTest(maxAttempts = 4, minSuccess = 2)
	void requiresTwoSuccesses() {
		// test code that must complete successfully twice
	}
	// end::retrying_configure_numbers_of_success[]

	// tag::suspending_between_each_retry[]
	@RetryingTest(maxAttempts = 3, suspendForMs = 100)
	void suspendsBetweenRetries() {
		// test code that will suspend/wait for 100 ms between retries
	}
	// end::suspending_between_each_retry[]

	class TheseTestsWillFailIntentionally {

		// tag::retrying_configure_exception_for_retry[]
		private int EXECUTION_COUNT;

		@RetryingTest(value = 3, onExceptions = IllegalArgumentException.class)
		void failsFirstWithExpectedThenWithUnexpectedException() {
			EXECUTION_COUNT++;
			if (EXECUTION_COUNT == 1) {
				throw new IllegalArgumentException();
			}
			if (EXECUTION_COUNT == 2) {
				throw new NullPointerException();
			}
		}

		// end::retrying_configure_exception_for_retry[]
	}

}
