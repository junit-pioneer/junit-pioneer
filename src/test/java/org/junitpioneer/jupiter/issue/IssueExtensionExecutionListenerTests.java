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
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junitpioneer.jupiter.issue.IssueExtensionExecutionListener.REPORT_ENTRY_KEY;
import static org.junitpioneer.jupiter.issue.TestPlanHelper.createTestIdentifier;

import java.util.Collections;
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

	private final IssueExtensionExecutionListener executionListener = new IssueExtensionExecutionListener();
	private final TestPlan testPlan = TestPlan.from(Collections.emptyList());

	@Test
	void noIssueTestCasesCreated() {
		executionListener.testPlanExecutionStarted(testPlan);
		executionListener.testPlanExecutionFinished(testPlan);

		List<IssueTestSuite> allTests = executionListener.createIssueTestSuites();
		assertThat(allTests).isEmpty();
	}

	@Test
	void issueTestCasesCreated() {
		ReportEntry successfulTestEntry = ReportEntry.from(REPORT_ENTRY_KEY, "successfulTest");
		TestIdentifier successfulTest = createTestIdentifier("t1");
		testPlan.add(successfulTest);

		executionListener.testPlanExecutionStarted(testPlan);
		executionListener.executionStarted(successfulTest);
		executionListener.executionFinished(successfulTest, TestExecutionResult.successful());
		executionListener.reportingEntryPublished(successfulTest, successfulTestEntry);
		executionListener.testPlanExecutionFinished(testPlan);

		// Verify result
		List<IssueTestSuite> allTests = executionListener.createIssueTestSuites();
		assertThat(allTests.size()).isEqualTo(1);

		IssueTestSuite issueTestSuite = allTests.get(0);
		assertAll(() -> assertThat(issueTestSuite.issueId()).isEqualTo("successfulTest"),
			() -> assertThat(issueTestSuite.tests().size()).isEqualTo(1));

		IssueTestCase testCase = issueTestSuite.tests().get(0);
		assertAll(() -> assertThat(testCase.testId()).isEqualTo("[test:t1]"),
			() -> assertThat(testCase.result()).isEqualTo(Status.SUCCESSFUL));
	}

	@Test
	void unknownTestResult() {
		ReportEntry unknownResultTestEntry = ReportEntry.from(REPORT_ENTRY_KEY, "abortedTest");
		TestIdentifier abortedTest = createTestIdentifier("tu");
		testPlan.add(abortedTest);

		executionListener.testPlanExecutionStarted(testPlan);
		executionListener.executionStarted(abortedTest);
		executionListener.executionFinished(abortedTest, TestExecutionResult.aborted(new RuntimeException()));
		executionListener.reportingEntryPublished(abortedTest, unknownResultTestEntry);
		executionListener.testPlanExecutionFinished(testPlan);

		// Verify result
		List<IssueTestSuite> allTests = executionListener.createIssueTestSuites();
		assertThat(allTests.size()).isEqualTo(1);

		IssueTestSuite issueTestSuite = allTests.get(0);
		assertAll(() -> assertThat(issueTestSuite.issueId()).isEqualTo("abortedTest"),
			() -> assertThat(issueTestSuite.tests().size()).isEqualTo(1));

		IssueTestCase testCase = issueTestSuite.tests().get(0);

		assertAll(() -> assertThat(testCase.testId()).isEqualTo("[test:tu]"),
			() -> assertThat(testCase.result()).isEqualTo(Status.ABORTED));
	}

}
