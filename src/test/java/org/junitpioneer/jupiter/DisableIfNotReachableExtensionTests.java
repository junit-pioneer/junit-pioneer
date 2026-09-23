/*
 * Copyright 2026 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

@DisplayName("Tests for the DisableIfNotReachable extension")
public class DisableIfNotReachableExtensionTests {

	@Test
	@DisplayName("Should enable test without annotation")
	void shouldEnableTestWithoutAnnotation() {
		final ExecutionResults results = PioneerTestKit
				.executeTestMethod(DisableIfNotReachableTestCases.class, "testNoAnnotation");

		assertThat(results).hasSingleStartedTest();
		assertThat(results).hasSingleSucceededTest();
		assertThat(results).hasNumberOfSkippedTests(0);
		assertThat(results).hasNoReportEntries();
	}

	@Test
	@DisplayName("Should enable test for reachable url")
	void shouldEnableTestWithReachableUrl() {
		final ExecutionResults results = PioneerTestKit
				.executeTestMethod(DisableIfNotReachableTestCases.class, "testReachableUrl");

		assertThat(results).hasSingleStartedTest();
		assertThat(results).hasSingleSucceededTest();
		assertThat(results).hasNumberOfSkippedTests(0);
		assertThat(results).hasNoReportEntries();
	}

	@Test
	@DisplayName("Should disable test for unreachable url")
	void shouldDisableTestWithUnreachableUrl() {
		final ExecutionResults results = PioneerTestKit
				.executeTestMethod(DisableIfNotReachableTestCases.class, "testUnreachableUrl");

		assertThat(results).hasNumberOfFailedTests(0);
		assertThat(results).hasNumberOfSucceededTests(0);
		assertThat(results).hasNumberOfSkippedTests(1);
		assertThat(results).hasNoReportEntries();
	}

	@Nested
	@DisplayName("Should throw an extension configuration exception")
	class BadConfigurationTests {

		@Test
		@DisplayName("for an empty url")
		void shouldThrowForEmptyUrl() {
			final ExecutionResults results = PioneerTestKit
					.executeTestMethod(DisableIfNotReachableTestCases.class, "testEmptyUrl");

			assertThat(results)
					.hasSingleFailedTest()
					.withException()
					.hasCauseInstanceOf(ExtensionConfigurationException.class)
					.hasMessageEndingWith("URL can not be empty");
		}

		@Test
		@DisplayName("for a completely invalid url")
		void shouldThrowForInvalidUrl() {
			final ExecutionResults results = PioneerTestKit
					.executeTestMethod(DisableIfNotReachableTestCases.class, "testInvalidUrl");

			assertThat(results)
					.hasSingleFailedTest()
					.withException()
					.hasCauseInstanceOf(ExtensionConfigurationException.class)
					.hasMessageEndingWith("URL ^invalidUrl^ is invalid");
		}

		@Test
		@DisplayName("for an valid url with missing scheme")
		void shouldThrowForMissingScheme() {
			final ExecutionResults results = PioneerTestKit
					.executeTestMethod(DisableIfNotReachableTestCases.class, "testNoScheme");

			assertThat(results)
					.hasSingleFailedTest()
					.withException()
					.hasCauseInstanceOf(ExtensionConfigurationException.class)
					.hasMessageEndingWith("Scheme for URL junit-pioneer.org must be http or https");
		}

		@Test
		@DisplayName("for an valid url with a non-http(s) scheme")
		void shouldThrowForInvalidScheme() {
			final ExecutionResults results = PioneerTestKit
					.executeTestMethod(DisableIfNotReachableTestCases.class, "testInvalidScheme");

			assertThat(results)
					.hasSingleFailedTest()
					.withException()
					.hasCauseInstanceOf(ExtensionConfigurationException.class)
					.hasMessageEndingWith("Scheme for URL file://junit-pioneer.org must be http or https");
		}

		@Test
		@DisplayName("for negative timeout")
		void shouldThrowForNegativeTimeout() {
			final ExecutionResults results = PioneerTestKit
					.executeTestMethod(DisableIfNotReachableTestCases.class, "testNegativeTimeout");

			assertThat(results)
					.hasSingleFailedTest()
					.withException()
					.hasCauseInstanceOf(ExtensionConfigurationException.class)
					.hasMessageEndingWith("Timeout must be greater than zero");
		}

	}

	static class DisableIfNotReachableTestCases {

		@Test
		void testNoAnnotation() {
		}

		@Test
		@DisableIfNotReachable
		void testEmptyUrl() {
		}

		@Test
		@DisableIfNotReachable(url = "^invalidUrl^")
		void testInvalidUrl() {
		}

		@Test
		@DisableIfNotReachable(url = "junit-pioneer.org")
		void testNoScheme() {
		}

		@Test
		@DisableIfNotReachable(url = "https://junit-pioneer")
		void testNoTopLevelDomain() {
		}

		@Test
		@DisableIfNotReachable(url = "file://junit-pioneer.org")
		void testInvalidScheme() {
		}

		@Test
		@DisableIfNotReachable(url = "https://junit-pioneer.org", timeoutMillis = -10)
		void testNegativeTimeout() {
		}

		@Test
		@DisableIfNotReachable(url = "https://junit-pioneer.org")
		void testReachableUrl() {
		}

		@Test
		@DisableIfNotReachable(url = "https://localhost")
		void testUnreachableUrl() {
		}

	}

}
