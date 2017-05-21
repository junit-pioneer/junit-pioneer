/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit$pioneer.jupiter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import org.junit$pioneer.AbstractPioneerTestEngineTests;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;

/**
 * These integration tests are incomplete,
 * see {@link OsConditionTests} for a detailed battery of unit tests.
 */
class OsConditionSmokeTests extends AbstractPioneerTestEngineTests {

	@Test
	void disabledOnLinux_onLinux_disabled() {
		Assumptions.assumeTrue(OS.determine() == OS.LINUX);

		ExecutionEventRecorder eventRecorder = executeTests(DisabledOnLinuxTestCase.class);

		assertEquals(1, eventRecorder.getContainerSkippedCount(), "# container skipped");
		assertEquals(0, eventRecorder.getTestStartedCount(), "# tests started");
	}

	@Test
	void disabledOnLinux_notOnLinux_enabled() {
		Assumptions.assumeTrue(OS.determine() != OS.LINUX);

		ExecutionEventRecorder eventRecorder = executeTests(DisabledOnLinuxTestCase.class);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
	}

	@Test
	void enabledOnLinux_onLinux_disabled() {
		Assumptions.assumeTrue(OS.determine() == OS.LINUX);

		ExecutionEventRecorder eventRecorder = executeTests(EnabledOnLinuxTestCase.class);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
	}

	@Test
	void enabledOnLinux_notOnLinux_disabled() {
		Assumptions.assumeTrue(OS.determine() != OS.LINUX);

		ExecutionEventRecorder eventRecorder = executeTests(EnabledOnLinuxTestCase.class);

		assertEquals(1, eventRecorder.getContainerSkippedCount(), "# container skipped");
		assertEquals(0, eventRecorder.getTestStartedCount(), "# tests started");
	}

	@Test
	void enabledAndDisabled_onAnyOs_someEnabledSomeDisabled() throws Exception {
		ExecutionEventRecorder eventRecorder = executeTests(EnabledAndDisabledTestMethods.class);

		// The test class contains three pairs of methods, where exactly one
		// method from each pair should be executed. Hence across all three OS
		// exactly three methods must be executed and three more skipped.

		assertEquals(3, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(3, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(3, eventRecorder.getTestSkippedCount(), "# tests skipped");
	}

	// TEST CASES -------------------------------------------------------------------

	@DisabledOnOs(OS.LINUX)
	private static class DisabledOnLinuxTestCase {

		@Test
		void unconditionalTest() {
		}

	}

	@EnabledOnOs(OS.LINUX)
	private static class EnabledOnLinuxTestCase {

		@Test
		void unconditionalTest() {
		}

	}

	private static class EnabledAndDisabledTestMethods {

		@DisabledOnOs(OS.LINUX)
		@Test
		void disabledOnLinuxTest() {
		}

		@EnabledOnOs(OS.LINUX)
		@Test
		void enabledOnLinuxTest() {
		}

		@DisabledOnOs(OS.WINDOWS)
		@Test
		void disabledOnWindowsTest() {
		}

		@EnabledOnOs(OS.WINDOWS)
		@Test
		void enabledOnWindowsTest() {
		}

		@DisabledOnOs(OS.MAC)
		@Test
		void disabledOnMacTest() {
		}

		@EnabledOnOs(OS.MAC)
		@Test
		void enabledOnMacTest() {
		}

	}

}
