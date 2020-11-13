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
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junitpioneer.jupiter.IssueTestCase;
import org.junitpioneer.jupiter.TestedIssue;

@Execution(ExecutionMode.SAME_THREAD)
public class IssueExtensionExecutionListenerTests {

	private final IssueExtensionExecutionListener sut = new IssueExtensionExecutionListener();
	private final TestPlan testPlan = TestPlan.from(Collections.emptyList());

	@Test
	void noIssueTestCasesCreated() {
		sut.testPlanExecutionStarted(testPlan);
		sut.testPlanExecutionFinished(testPlan);

		ConcurrentHashMap<String, TestedIssue> allIssuedTests = sut.allTestedIssues;
		assertThat(allIssuedTests).isEmpty();
	}

	@Test
	void issueTestCasesCreated() {

		ReportEntry successfulTestEntry = ReportEntry.from(REPORT_ENTRY_KEY, "successfulTest");

		TestIdentifier successfulTest = createTestIdentifier("t1");
		testPlan.add(successfulTest);

		sut.testPlanExecutionStarted(testPlan);

		sut.executionStarted(successfulTest);
		sut.executionFinished(successfulTest, TestExecutionResult.successful());
		sut.reportingEntryPublished(successfulTest, successfulTestEntry);

		sut.testPlanExecutionFinished(testPlan);

		// Verify result
		ConcurrentHashMap<String, TestedIssue> allIssuedTests = sut.allTestedIssues;

		assertThat(allIssuedTests.size()).isEqualTo(1);

		TestedIssue testedIssue = allIssuedTests.get("successfulTest");

		assertAll(() -> assertThat(testedIssue.getIssueId()).isEqualTo("successfulTest"),
			() -> assertThat(testedIssue.getAllTests().size()).isEqualTo(1));

		IssueTestCase testCase = testedIssue.getAllTests().get(0);

		assertAll(() -> assertThat(testCase.getUniqueName()).isEqualTo("[test:t1]"),
			() -> assertThat(testCase.getResult()).isEqualTo("SUCCESSFUL"));
	}

	@Test
	void unknownTestResult() {
		ReportEntry unknownResultTestEntry = ReportEntry.from(REPORT_ENTRY_KEY, "unknownResultTest");

		TestIdentifier unknownResultTest = createTestIdentifier("tu");
		testPlan.add(unknownResultTest);

		sut.testPlanExecutionStarted(testPlan);

		sut.executionStarted(unknownResultTest);
		sut.reportingEntryPublished(unknownResultTest, unknownResultTestEntry);

		sut.testPlanExecutionFinished(testPlan);

		// Verify result
		ConcurrentHashMap<String, TestedIssue> allIssuedTests = sut.allTestedIssues;

		assertThat(allIssuedTests.size()).isEqualTo(1);

		TestedIssue testedIssue = allIssuedTests.get("unknownResultTest");

		assertAll(() -> assertThat(testedIssue.getIssueId()).isEqualTo("unknownResultTest"),
			() -> assertThat(testedIssue.getAllTests().size()).isEqualTo(1));

		IssueTestCase testCase = testedIssue.getAllTests().get(0);

		assertAll(() -> assertThat(testCase.getUniqueName()).isEqualTo("[test:tu]"),
			() -> assertThat(testCase.getResult()).isEqualTo("UNKNOWN"));
	}

}
