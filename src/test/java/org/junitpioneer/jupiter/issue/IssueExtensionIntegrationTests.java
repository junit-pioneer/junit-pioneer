/*
 * Copyright 2015-2020 the original author or authors.
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
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junitpioneer.jupiter.Issue;
import org.junitpioneer.jupiter.IssueTestSuite;
import org.opentest4j.TestAbortedException;

/**
 * Mary Elizabeth Fyre: Do Not Stand at My Grave and Weep is in the public domain.
 */
public class IssueExtensionIntegrationTests {

	private final StoringIssueProcessor issueProcessor = (StoringIssueProcessor) IssueProcessorFactory
			.getIssueProcessors()
			.iterator()
			.next();

	@Test
	void testIssueCases() {
		LauncherFactory
				.create()
				.execute(LauncherDiscoveryRequestBuilder
						.request()
						.selectors(DiscoverySelectors.selectClass(IssueIntegrationCases.class))
						.build());

		List<IssueTestSuite> issueTestSuites = issueProcessor.issueTestSuites();

		assertThat(issueTestSuites).isNotEmpty();
	}

	static class IssueIntegrationCases {

		@Test
		@Issue("Do not stand at my grave and weep. I am not there. I do not sleep.")
		void successfulTest() {
		}

		@Test
		@Issue("I am a thousand winds that blow. I am the diamond glints on snow.")
		void failingTest() {
			fail("supposed to fail");
		}

		@Test
		@Issue("I am the sunlight on ripened grain. I am the gentle autumn rain.")
		void abortedTest() {
			throw new TestAbortedException();
		}

		@Test
		@Disabled
		@Issue("When you awaken in the morning's hush, I am the swift uplifting rush")
		void skippedTest() {
		}

		@Test
		@Issue("Of quiet birds in circled flight. I am the soft stars that shine at night.")
		void publishingTest(TestReporter reporter) {
			reporter.publishEntry("Issue", "reporting test");
		}

		@Test
		@DisplayName("Do not stand at my grave and cry; I am not there. I did not die.")
		void nonIssueTest() {
		}

	}

}
