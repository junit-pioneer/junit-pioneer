/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.issue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.platform.engine.TestExecutionResult.Status;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junitpioneer.jupiter.Issue;
import org.junitpioneer.jupiter.IssueTestCase;
import org.junitpioneer.jupiter.IssueTestSuite;
import org.opentest4j.TestAbortedException;

/**
 * Mary Elizabeth Fyre: Do Not Stand at My Grave and Weep is in the public domain.
 */
public class IssueExtensionIntegrationTests {

	@Test
	void testIssueCases() {
		LauncherFactory
				.create()
				.execute(LauncherDiscoveryRequestBuilder
						.request()
						.selectors(DiscoverySelectors.selectClass(IssueIntegrationCases.class))
						.build());

		List<IssueTestSuite> issueTestSuites = StoringIssueProcessor.ISSUE_TEST_SUITES;

		assertThat(issueTestSuites).hasSize(3);
		assertThat(issueTestSuites)
				.extracting(IssueTestSuite::issueId)
				.containsExactlyInAnyOrder("Poem #1", "Poem #2", "Poem #3");
		assertThat(issueTestSuites)
				.allSatisfy(issueTestSuite -> assertThat(issueTestSuite.tests())
						.allSatisfy(IssueExtensionIntegrationTests::assertStatus));
	}

	private static void assertStatus(IssueTestCase testCase) {
		if (testCase.testId().contains("successful") || testCase.testId().contains("publishing"))
			assertThat(testCase.result()).isEqualTo(Status.SUCCESSFUL);
		if (testCase.testId().contains("aborted"))
			assertThat(testCase.result()).isEqualTo(Status.ABORTED);
		if (testCase.testId().contains("failing"))
			assertThat(testCase.result()).isEqualTo(Status.FAILED);
	}

	static class IssueIntegrationCases {

		@Test
		@Issue("Poem #1")
		@DisplayName("Do not stand at my grave and weep. I am not there. I do not sleep.")
		void successfulTest() {
		}

		@Test
		@Issue("Poem #1")
		@DisplayName("I am a thousand winds that blow. I am the diamond glints on snow.")
		void failingTest() {
			fail("supposed to fail");
		}

		@Test
		@Issue("Poem #2")
		@DisplayName("I am the sunlight on ripened grain. I am the gentle autumn rain.")
		void abortedTest() {
			throw new TestAbortedException();
		}

		@Test
		@Issue("Poem #2")
		@Disabled("skipped")
		@DisplayName("When you awaken in the morning's hush, I am the swift uplifting rush")
		void skippedTest() {
		}

		@Test
		@Issue("Poem #3")
		@DisplayName("Of quiet birds in circled flight. I am the soft stars that shine at night.")
		void publishingTest(TestReporter reporter) {
			reporter.publishEntry("Issue", "reporting test");
		}

		@Test
		@DisplayName("Do not stand at my grave and cry; I am not there. I did not die.")
		void nonIssueTest() {
		}

	}

}
