/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.codefx.junit.io.vintage;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codefx.junit.io.vintage.ExpectedExceptionExtension.EXPECTED_EXCEPTION_WAS_NOT_THROWN;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.InvalidPathException;
import java.util.Optional;

import org.codefx.junit.io.AbstractIoTestEngineTests;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;

public class TestIntegrationTest extends AbstractIoTestEngineTests {

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

	// TEST CASES -------------------------------------------------------------------

	static class TestTestCase {

		@Test
		void test_successfulTest() {
			assertTrue(true);
		}

		@Test
		void test_exceptionThrown() {
			throw new IllegalArgumentException();
		}

		@Test(expected = IllegalArgumentException.class)
		void testWithExpectedException_successfulTest() {
			assertTrue(true);
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

	}

}
