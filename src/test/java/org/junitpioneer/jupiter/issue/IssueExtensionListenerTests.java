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
import static org.junitpioneer.jupiter.issue.TestPlanHelper.createTestIdentifier;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junitpioneer.jupiter.IssuedTestCase;

@Execution(ExecutionMode.SAME_THREAD)
public class IssueExtensionListenerTests {

	private final IssueExtensionExecutionListener sut = new IssueExtensionExecutionListener();
	private final TestPlan testPlan = TestPlan.from(Collections.emptyList());

	@Test
	void noIssueTestCasesCreated() {
		sut.testPlanExecutionStarted(testPlan);
		sut.testPlanExecutionFinished(testPlan);

		List<IssuedTestCase> allIssuedTests = sut.allIssuedTests;
		assertThat(allIssuedTests).isEmpty();
	}

	@Test
	void issueTestCasesCreated() {

		ReportEntry successfulTestEntry = ReportEntry.from("Issue", "successfulTest");

		TestIdentifier successfulTest = createTestIdentifier("t1");
		testPlan.add(successfulTest);

		sut.testPlanExecutionStarted(testPlan);

		sut.executionStarted(successfulTest);
		sut.executionFinished(successfulTest, TestExecutionResult.successful());
		sut.reportingEntryPublished(successfulTest, successfulTestEntry);

		sut.testPlanExecutionFinished(testPlan);

		// Verify result
		List<IssuedTestCase> allIssuedTests = sut.allIssuedTests;

		assertThat(allIssuedTests.size()).isEqualTo(1);

		IssuedTestCase issuedTestCase = allIssuedTests.get(0);

		assertAll(() -> assertThat(issuedTestCase.getUniqueName()).isEqualTo("[test:t1]"),
			() -> assertThat(issuedTestCase.getIssueId()).isEqualTo("successfulTest"),
			() -> assertThat(issuedTestCase.getResult()).isEqualTo("SUCCESSFUL"));
	}

	@Test
	void unknownTestResult() {
		ReportEntry unknownResultTestEntry = ReportEntry.from("Issue", "unknownResultTest");

		TestIdentifier unknownResultTest = createTestIdentifier("tu");
		testPlan.add(unknownResultTest);

		sut.testPlanExecutionStarted(testPlan);

		sut.executionStarted(unknownResultTest);
		sut.reportingEntryPublished(unknownResultTest, unknownResultTestEntry);

		sut.testPlanExecutionFinished(testPlan);

		// Verify result
		List<IssuedTestCase> allIssuedTests = sut.allIssuedTests;

		assertThat(allIssuedTests.size()).isEqualTo(1);

		IssuedTestCase issuedTestCase = allIssuedTests.get(0);

		assertAll(() -> assertThat(issuedTestCase.getUniqueName()).isEqualTo("[test:tu]"),
			() -> assertThat(issuedTestCase.getIssueId()).isEqualTo("unknownResultTest"),
			() -> assertThat(issuedTestCase.getResult()).isEqualTo("UNKNOWN"));
	}

}
