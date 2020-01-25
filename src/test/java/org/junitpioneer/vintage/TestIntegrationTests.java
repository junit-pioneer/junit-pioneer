/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.vintage;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junitpioneer.vintage.ExpectedExceptionExtension.EXPECTED_EXCEPTION_WAS_NOT_THROWN;

import java.nio.file.InvalidPathException;
import java.util.Optional;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junitpioneer.AbstractPioneerTestEngineTests;

/**
 * Tests the vintage {@link Test @Test} annotation by running the entire test engine.
 */
class TestIntegrationTests extends AbstractPioneerTestEngineTests {

	@org.junit.jupiter.api.Test
	void test_successfulTest_passes() throws Exception {
		ExecutionEventRecorder eventRecorder = executeTests(TestTestCase.class, "test_successfulTest");

		assertThat(eventRecorder.getTestStartedCount()).isEqualTo(1);
		assertThat(eventRecorder.getTestSuccessfulCount()).isEqualTo(1);
	}

	@org.junit.jupiter.api.Test
	void test_exceptionThrown_fails() throws Exception {
		ExecutionEventRecorder eventRecorder = executeTests(TestTestCase.class, "test_exceptionThrown");

		assertThat(eventRecorder.getTestStartedCount()).isEqualTo(1);
		assertThat(eventRecorder.getTestFailedCount()).isEqualTo(1);
	}

	// expected exception

	@org.junit.jupiter.api.Test
	void testWithExpectedException_successfulTest_fails() {
		ExecutionEventRecorder eventRecorder = executeTests(TestTestCase.class,
			"testWithExpectedException_successfulTest");

		assertThat(eventRecorder.getTestStartedCount()).isEqualTo(1);
		assertThat(eventRecorder.getTestFailedCount()).isEqualTo(1);

		//@formatter:off
		Optional<String> failedTestMessage = eventRecorder
				.getFailedTestFinishedEvents().get(0)
				.getPayload(TestExecutionResult.class)
				.flatMap(TestExecutionResult::getThrowable)
				.map(Throwable::getMessage);
		//@formatter:on
		String expectedMessage = format(EXPECTED_EXCEPTION_WAS_NOT_THROWN, IllegalArgumentException.class);
		assertThat(failedTestMessage).contains(expectedMessage);
	}

	@org.junit.jupiter.api.Test
	void testWithExpectedException_exceptionThrownOfRightType_passes() {
		ExecutionEventRecorder eventRecorder = executeTests(TestTestCase.class,
			"testWithExpectedException_exceptionThrownOfRightType");

		assertThat(eventRecorder.getTestStartedCount()).isEqualTo(1);
		assertThat(eventRecorder.getTestSuccessfulCount()).isEqualTo(1);
	}

	@org.junit.jupiter.api.Test
	void testWithExpectedException_exceptionThrownOfSubtype_passes() {
		ExecutionEventRecorder eventRecorder = executeTests(TestTestCase.class,
			"testWithExpectedException_exceptionThrownOfSubtype");

		assertThat(eventRecorder.getTestStartedCount()).isEqualTo(1);
		assertThat(eventRecorder.getTestSuccessfulCount()).isEqualTo(1);
	}

	@org.junit.jupiter.api.Test
	void testWithExpectedException_exceptionThrownOfSupertype_fails() {
		ExecutionEventRecorder eventRecorder = executeTests(TestTestCase.class,
			"testWithExpectedException_exceptionThrownOfSupertype");

		assertThat(eventRecorder.getTestStartedCount()).isEqualTo(1);
		assertThat(eventRecorder.getTestFailedCount()).isEqualTo(1);

		//@formatter:off
		Optional<Throwable> failedTestThrowable = eventRecorder
				.getFailedTestFinishedEvents().get(0)
				.getPayload(TestExecutionResult.class)
				.flatMap(TestExecutionResult::getThrowable);
		//@formatter:on
		assertThat(failedTestThrowable).containsInstanceOf(RuntimeException.class);
	}

	// timeout

	@org.junit.jupiter.api.Test
	void testWithTimeout_belowTimeout_passes() {
		ExecutionEventRecorder eventRecorder = executeTests(TestTestCase.class, "testWithTimeout_belowTimeout");

		assertThat(eventRecorder.getTestStartedCount()).isEqualTo(1);
		assertThat(eventRecorder.getTestSuccessfulCount()).isEqualTo(1);
	}

	@org.junit.jupiter.api.Test
	void testWithTimeout_exceedsTimeout_fails() throws Exception {
		ExecutionEventRecorder eventRecorder = executeTests(TestTestCase.class, "testWithTimeout_exceedsTimeout");

		assertThat(eventRecorder.getTestStartedCount()).isEqualTo(1);
		assertThat(eventRecorder.getTestFailedCount()).isEqualTo(1);

		//@formatter:off
		Optional<String> failedTestMessage = eventRecorder
				.getFailedTestFinishedEvents().get(0)
				.getPayload(TestExecutionResult.class)
				.flatMap(TestExecutionResult::getThrowable)
				.map(Throwable::getMessage);
		String expectedMessage = String.format(
				TimeoutExtension.TEST_RAN_TOO_LONG, "testWithTimeout_exceedsTimeout()", 1, 10);
		//@formatter:on
		// the message contains the actual run time, which is unpredictable, so it has to be cut off for the assertion
		String expectedKnownPrefix = expectedMessage.substring(0, expectedMessage.length() - 6);
		assertThat(failedTestMessage).isNotEmpty();
		assertThat(failedTestMessage.get()).startsWith(expectedKnownPrefix);
	}

	// TEST CASES -------------------------------------------------------------------

	static class TestTestCase {

		@Test
		void test_successfulTest() {
			assertThat(true).isTrue();
		}

		@Test
		void test_exceptionThrown() {
			throw new IllegalArgumentException();
		}

		// expected exception

		@Test(expected = IllegalArgumentException.class)
		void testWithExpectedException_successfulTest() {
			assertThat(true).isTrue();
		}

		@Test(expected = IllegalArgumentException.class)
		void testWithExpectedException_exceptionThrownOfRightType() {
			throw new IllegalArgumentException();
		}

		@Test(expected = IllegalArgumentException.class)
		void testWithExpectedException_exceptionThrownOfSubtype() {
			throw new InvalidPathException("", "");
		}

		@Test(expected = IllegalArgumentException.class)
		void testWithExpectedException_exceptionThrownOfSupertype() {
			throw new RuntimeException();
		}

		// timeout

		@Test(timeout = 10_000)
		void testWithTimeout_belowTimeout() {
			assertThat(true).isTrue();
		}

		@Test(timeout = 1)
		void testWithTimeout_exceedsTimeout() throws Exception {
			Thread.sleep(10);
		}

	}

}
