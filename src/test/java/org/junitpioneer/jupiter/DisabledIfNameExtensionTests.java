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
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junitpioneer.AbstractPioneerTestEngineTests;

class DisabledIfNameExtensionTests extends AbstractPioneerTestEngineTests {

	@Test
	void test_testShouldAlwaysBeDisabled() throws Exception {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(DisabledIfTests.class);

		assertThat(eventRecorder.getTestSkippedCount()).isEqualTo(5);
		assertThat(eventRecorder.getTestSuccessfulCount()).isEqualTo(1);
	}

	// TEST CASES -------------------------------------------------------------------

	static class DisabledIfTests {

		//@formatter:off
		@DisableIfName("disable")
		@ParameterizedTest(name = "See if enabled with {0}")
		@ValueSource(
				strings = {
						"disable who",
						"you, disable you",
						"why am I disabled",
						"what has been disabled must stay disabled",
						"fine disable me all you want",
						"not those one, though!"
				}
		)
		//@formatter:on
		void testWhereSomeExecutionsAreDisabled(String reason) {
			if (reason.contains("disable"))
				fail("Test Should've been disabled " + reason);
		}

	}

}
