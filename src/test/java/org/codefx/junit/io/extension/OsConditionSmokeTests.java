/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.codefx.junit.io.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

/**
 * These integration tests are incomplete,
 * see {@link OsConditionTests} for a detailed battery of unit tests.
 */
class OsConditionSmokeTests extends AbstractJupiterTestEngineTests {

	@Test
	void disabledOnNix_onNix_disabled() {
		Assumptions.assumeTrue(OS.determine() == OS.NIX);

		// @formatter:off
		LauncherDiscoveryRequest request = request()
				.selectors(selectClass(DisabledOnNixTestCase.class))
				.build();
		// @formatter:on
		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getContainerSkippedCount(), "# container skipped");
		assertEquals(0, eventRecorder.getTestStartedCount(), "# tests started");
	}

	@Test
	void disabledOnNix_notOnNix_enabled() {
		Assumptions.assumeTrue(OS.determine() != OS.NIX);

		// @formatter:off
		LauncherDiscoveryRequest request = request()
				.selectors(selectClass(DisabledOnNixTestCase.class))
				.build();
		// @formatter:on
		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
	}

	@Test
	void enabledOnNix_onNix_disabled() {
		Assumptions.assumeTrue(OS.determine() == OS.NIX);

		// @formatter:off
		LauncherDiscoveryRequest request = request()
				.selectors(selectClass(EnabledOnNixTestCase.class))
				.build();
		// @formatter:on
		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
	}

	@Test
	void enabledOnNix_notOnNix_disabled() {
		Assumptions.assumeTrue(OS.determine() != OS.NIX);

		// @formatter:off
		LauncherDiscoveryRequest request = request()
				.selectors(selectClass(EnabledOnNixTestCase.class))
				.build();
		// @formatter:on
		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getContainerSkippedCount(), "# container skipped");
		assertEquals(0, eventRecorder.getTestStartedCount(), "# tests started");
	}

	@Test
	void enabledAndDisabled_onAnyOs_someEnabledSomeDisabled() throws Exception {
		// @formatter:off
		LauncherDiscoveryRequest request = request()
				.selectors(selectClass(EnabledAndDisabledTestMethods.class))
				.build();
		// @formatter:on
		ExecutionEventRecorder eventRecorder = executeTests(request);

		// The test class contains three pairs of methods, where exactly one
		// method from each pair should be executed. Hence across all three OS
		// exactly three methods must be executed and three more skipped.

		assertEquals(3, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(3, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(3, eventRecorder.getTestSkippedCount(), "# tests skipped");
	}

	// TEST CASES -------------------------------------------------------------------

	@DisabledOnOs(OS.NIX)
	private static class DisabledOnNixTestCase {

		@Test
		void unconditionalTest() {
		}

	}

	@EnabledOnOs(OS.NIX)
	private static class EnabledOnNixTestCase {

		@Test
		void unconditionalTest() {
		}

	}

	private static class EnabledAndDisabledTestMethods {

		@DisabledOnOs(OS.NIX)
		@Test
		void disabledOnNixTest() {
		}

		@EnabledOnOs(OS.NIX)
		@Test
		void enabledOnNixTest() {
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
