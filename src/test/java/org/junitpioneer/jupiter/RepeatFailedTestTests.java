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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

class RepeatFailedTestTests {

	@Test
	void failsNever_executedOnce_passes() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(RepeatFailedTestTestCase.class, "failsNever");

		assertThat(results.numberOfDynamicRegisteredTests()).isEqualTo(1);
		assertThat(results.numberOfSucceededTests()).isEqualTo(1);
	}

	@Test
	void failsOnlyOnFirstInvocation_executedTwice_passes() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(RepeatFailedTestTestCase.class, "failsOnlyOnFirstInvocation");

		assertThat(results.numberOfDynamicRegisteredTests()).isEqualTo(2);
		assertThat(results.numberOfAbortedTests()).isEqualTo(1);
		assertThat(results.numberOfSucceededTests()).isEqualTo(1);
	}

	@Test
	void failsAlways_executedThreeTimes_fails() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(RepeatFailedTestTestCase.class, "failsAlways");

		assertThat(results.numberOfDynamicRegisteredTests()).isEqualTo(3);
		assertThat(results.numberOfAbortedTests()).isEqualTo(2);
		assertThat(results.numberOfFailedTests()).isEqualTo(1);
	}

	// TEST CASES -------------------------------------------------------------------

	static class RepeatFailedTestTestCase {

		private static int FAILS_ONLY_ON_FIRST_INVOCATION;

		@RepeatFailedTest(3)
		void failsNever() {
		}

		@RepeatFailedTest(3)
		void failsOnlyOnFirstInvocation() {
			FAILS_ONLY_ON_FIRST_INVOCATION++;
			if (FAILS_ONLY_ON_FIRST_INVOCATION == 1) {
				throw new IllegalArgumentException();
			}
		}

		@RepeatFailedTest(3)
		void failsAlways() {
			throw new IllegalArgumentException();
		}

	}

}
