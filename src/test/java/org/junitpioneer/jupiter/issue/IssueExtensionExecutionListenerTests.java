/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.issue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junitpioneer.jupiter.issue.IssueExtensionExecutionListener.REPORT_ENTRY_KEY;
import static org.junitpioneer.jupiter.issue.TestPlanHelper.createTestIdentifier;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestExecutionResult.Status;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junitpioneer.jupiter.IssueTestCase;
import org.junitpioneer.jupiter.IssueTestSuite;

@Execution(ExecutionMode.SAME_THREAD)
public class IssueExtensionExecutionListenerTests {

	// when debugging, be aware that the service loader also created instances of the listener;
	// we can't use it here, because the running Jupiter instance uses it to gather test information
	private final IssueExtensionExecutionListener executionListener = new IssueExtensionExecutionListener();
	// TestPlan only offers a single public but internal factory method that changed in JUnit v5.8.0-RC1
	// since we test with multiple JUnit versions, we mock this class to circumvent the problem of creating it
	private final TestPlan testPlan = mock(TestPlan.class);

	@Test
	void noIssueTestCasesCreated() {
		executionListener.testPlanExecutionStarted(testPlan);
		executionListener.testPlanExecutionFinished(testPlan);

		List<IssueTestSuite> allTests = executionListener.createIssueTestSuites();
		assertThat(allTests).isEmpty();
	}

	@Test
	void issueTestCasesCreated() {
		ReportEntry issueEntry = ReportEntry.from(REPORT_ENTRY_KEY, "#123");
		TestIdentifier successfulTest = createTestIdentifier("successful-test");

		executionListener.testPlanExecutionStarted(testPlan);
		executionListener.reportingEntryPublished(successfulTest, issueEntry);
		executionListener.executionStarted(successfulTest);
		executionListener.executionFinished(successfulTest, TestExecutionResult.successful());
		executionListener.testPlanExecutionFinished(testPlan);

		// Verify result
		List<IssueTestSuite> allTests = executionListener.createIssueTestSuites();
		assertThat(allTests.size()).isEqualTo(1);

		IssueTestSuite issueTestSuite = allTests.get(0);
		assertAll(() -> assertThat(issueTestSuite.issueId()).isEqualTo("#123"),
			() -> assertThat(issueTestSuite.tests().size()).isEqualTo(1));

		assertThat(issueTestSuite.tests())
				.containsExactly(new IssueTestCase("[test:successful-test]", Status.SUCCESSFUL));
	}

	@Test
	void abortedIssueTestCaseCreated() {
		ReportEntry issueEntry = ReportEntry.from(REPORT_ENTRY_KEY, "#123");
		TestIdentifier abortedTest = createTestIdentifier("aborted-test");

		executionListener.testPlanExecutionStarted(testPlan);
		executionListener.reportingEntryPublished(abortedTest, issueEntry);
		executionListener.executionStarted(abortedTest);
		executionListener.executionFinished(abortedTest, TestExecutionResult.aborted(new RuntimeException()));
		executionListener.testPlanExecutionFinished(testPlan);

		// Verify result
		List<IssueTestSuite> allTests = executionListener.createIssueTestSuites();
		assertThat(allTests.size()).isEqualTo(1);

		IssueTestSuite issueTestSuite = allTests.get(0);
		assertAll(() -> assertThat(issueTestSuite.issueId()).isEqualTo("#123"),
			() -> assertThat(issueTestSuite.tests().size()).isEqualTo(1));

		assertThat(issueTestSuite.tests()).containsExactly(new IssueTestCase("[test:aborted-test]", Status.ABORTED));
	}

	@Test
	void multipleIssueTestCasesCreated() {
		ReportEntry issueEntry = ReportEntry.from(REPORT_ENTRY_KEY, "#123");
		TestIdentifier successfulTest = createTestIdentifier("successful-test");
		TestIdentifier abortedTest = createTestIdentifier("aborted-test");

		executionListener.testPlanExecutionStarted(testPlan);
		executionListener.reportingEntryPublished(successfulTest, issueEntry);
		executionListener.executionStarted(successfulTest);
		executionListener.executionFinished(successfulTest, TestExecutionResult.successful());
		executionListener.reportingEntryPublished(abortedTest, issueEntry);
		executionListener.executionStarted(abortedTest);
		executionListener.executionFinished(abortedTest, TestExecutionResult.aborted(new RuntimeException()));
		executionListener.testPlanExecutionFinished(testPlan);

		// Verify result
		List<IssueTestSuite> allTests = executionListener.createIssueTestSuites();
		assertThat(allTests.size()).isEqualTo(1);

		IssueTestSuite issueTestSuite = allTests.get(0);
		assertAll(() -> assertThat(issueTestSuite.issueId()).isEqualTo("#123"),
			() -> assertThat(issueTestSuite.tests().size()).isEqualTo(2));

		assertThat(issueTestSuite.tests())
				.containsExactlyInAnyOrder(new IssueTestCase("[test:successful-test]", Status.SUCCESSFUL),
					new IssueTestCase("[test:aborted-test]", Status.ABORTED));
	}

}
