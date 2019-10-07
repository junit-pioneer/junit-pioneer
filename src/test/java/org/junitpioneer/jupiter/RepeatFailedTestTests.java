/*
 * Copyright 2015-2018 the original author or authors.
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
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junitpioneer.AbstractPioneerTestEngineTests;

class RepeatFailedTestTests extends AbstractPioneerTestEngineTests {

	@Test
	void failsNever_executedOnce_passes() throws Exception {
		ExecutionEventRecorder eventRecorder = executeTests(RepeatFailedTestTestCase.class, "failsNever");

		assertThat(eventRecorder.getDynamicTestRegisteredCount()).isEqualTo(1);
		assertThat(eventRecorder.getTestSuccessfulCount()).isEqualTo(1);
	}

	@Test
	void failsOnlyOnFirstInvocation_executedTwice_passes() throws Exception {
		ExecutionEventRecorder eventRecorder = executeTests(RepeatFailedTestTestCase.class,
			"failsOnlyOnFirstInvocation");

		assertThat(eventRecorder.getDynamicTestRegisteredCount()).isEqualTo(2);
		assertThat(eventRecorder.getTestAbortedCount()).isEqualTo(1);
		assertThat(eventRecorder.getTestSuccessfulCount()).isEqualTo(1);
	}

	@Test
	void failsAlways_executedThreeTimes_fails() throws Exception {
		ExecutionEventRecorder eventRecorder = executeTests(RepeatFailedTestTestCase.class, "failsAlways");

		assertThat(eventRecorder.getDynamicTestRegisteredCount()).isEqualTo(3);
		assertThat(eventRecorder.getTestAbortedCount()).isEqualTo(2);
		assertThat(eventRecorder.getTestFailedCount()).isEqualTo(1);
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
