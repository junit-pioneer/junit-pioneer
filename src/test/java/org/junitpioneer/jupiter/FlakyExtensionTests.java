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

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junitpioneer.testkit.PioneerTestKit.executeTestMethod;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junitpioneer.testkit.ExecutionResults;

public class FlakyExtensionTests {

	@Test
	@DisplayName("completely disregards Flaky annotation if initial Test is successful")
	void disregarded() {
		ExecutionResults results = executeTestMethod(FlakyTestCases.class, "negativeValuePassing");

		assertThat(results).hasNumberOfSucceededContainers(2);
		assertThat(results).hasSingleSucceededTest();
	}

	@Test
	@DisplayName("throws an ExtensionConfigurationException for negative value if initial Test is failing")
	void invalidValue() {
		ExecutionResults results = executeTestMethod(FlakyTestCases.class, "negativeValueFailing");

		assertThat(results).hasNumberOfAbortedTests(1);
		assertThat(results).hasSingleFailedContainer().withExceptionInstanceOf(ExtensionConfigurationException.class);
	}

	@Test
	@DisplayName("failing test annotated with @Flaky and value 3 should run three times, abort twice and fail once")
	void alwaysFailing() {
		ExecutionResults results = executeTestMethod(FlakyTestCases.class, "alwaysFail");

		assertThat(results).hasNumberOfAbortedTests(2);
		assertThat(results)
				.hasSingleFailedTest()
				.withExceptionInstanceOf(IllegalStateException.class)
				.hasMessage("Always failing");
	}

	@Test
	@DisplayName("once failing test annotated with @Flaky and value 3 should run three times, abort once then succeed")
	void failsOnlyFirstTime() {
		ExecutionResults results = executeTestMethod(FlakyTestCases.class, "failsOnlyOnFirstInvocation");

		assertThat(results).hasSingleAbortedTest(); // failed, retried
		assertThat(results).hasSingleSucceededTest(); // succeeded
		assertThat(results).hasSingleSkippedTest(); // already succeeded, this invocation is skipped
	}

	@TestInstance(PER_CLASS)
	static class FlakyTestCases {

		private int executionCount;

		@Test
		@Flaky(value = -1)
		void negativeValuePassing() {
		}

		@Test
		@Flaky(-1)
		void negativeValueFailing() {
			Assertions.fail();
		}

		@Test
		@Flaky(3)
		void alwaysFail() {
			throw new IllegalStateException("Always failing");
		}

		@Test
		@Flaky(3)
		void failsOnlyOnFirstInvocation() {
			executionCount++;
			if (executionCount == 1) {
				throw new IllegalArgumentException();
			}
		}

	}

}
