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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.codefx.junit.io.AbstractIoTestEngineTests;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;

public class TestIntegrationTest extends AbstractIoTestEngineTests {

	@org.junit.jupiter.api.Test
	void test_successfulTest_passes() throws Exception {
		ExecutionEventRecorder eventRecorder = executeTests(TestTestCase.class, "test_successfulTest");

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
	}

	@org.junit.jupiter.api.Test
	void test_exceptionThrown_fails() throws Exception {
		ExecutionEventRecorder eventRecorder = executeTests(TestTestCase.class, "test_exceptionThrown");

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed");
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

		@Test
		void testWithExpectedException_successfulTest() {
			assertTrue(true);
		}

	}

}
