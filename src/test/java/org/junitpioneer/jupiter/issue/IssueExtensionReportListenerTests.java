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

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

@DisplayName("IssueExtensionReportListener ")
@Execution(ExecutionMode.SAME_THREAD)
public class IssueExtensionReportListenerTests {

	private final IssueExtensionListener sut = new IssueExtensionListener();
	private final TestPlan testPlan = TestPlan.from(Collections.emptyList());

	@Nested
	@DisplayName("writeReport method ")
	class CreateReportTests {

		@DisplayName("throws and IssueExtensionReportException when an exception was caught")
		@Test
		void throwRuntimeExceptionWhileCreatingReport() {
			Throwable thrown = Assertions.assertThrows(Throwable.class, () -> {
				sut.writeReport(null);
			});

			assertThat(thrown).isExactlyInstanceOf(IssueExtensionReportException.class);
			assertThat(thrown.getMessage()).isEqualTo("Error while creating the pioneer report");
			assertThat(thrown.getCause()).isExactlyInstanceOf(IllegalArgumentException.class);

		}

		@DisplayName("creates an empty report, if no container or test were executed and no entry was published")
		@Test
		void emptyReport() {
			sut.testPlanExecutionStarted(testPlan);
			sut.testPlanExecutionFinished(testPlan);

			// Verify result
			IssueReport report = sut.issueReport;
			assertThat(report.getIssues().getIssue().isEmpty()).isTrue();
		}

		@DisplayName("creates a report with test containers and tests with published entries as their properties")
		@Test
		void reportingCorrectCounts() {

			ReportEntry successfulTestEntry = ReportEntry.from("Issue", "succTest");

			TestIdentifier successfulContainer = createContainerIdentifier("c1");
			TestIdentifier failedContainer = createContainerIdentifier("c2");
			TestIdentifier abortedContainer = createContainerIdentifier("c3");
			TestIdentifier skippedContainer = createContainerIdentifier("c4");

			TestIdentifier successfulTest = createTestIdentifier("t1");
			TestIdentifier failedTest = createTestIdentifier("t2");
			TestIdentifier abortedTest = createTestIdentifier("t3");
			TestIdentifier skippedTest = createTestIdentifier("t4");

			sut.testPlanExecutionStarted(testPlan);

			sut.executionSkipped(skippedContainer, "skipped");
			sut.executionSkipped(skippedTest, "skipped");

			sut.executionStarted(successfulContainer);
			sut.executionFinished(successfulContainer, TestExecutionResult.successful());

			sut.executionStarted(successfulTest);
			sut.executionFinished(successfulTest, TestExecutionResult.successful());

			sut.reportingEntryPublished(successfulTest, successfulTestEntry);

			sut.executionStarted(failedContainer);
			sut.executionFinished(failedContainer, TestExecutionResult.failed(new RuntimeException("failed")));

			sut.executionStarted(failedTest);
			sut.executionFinished(failedTest, TestExecutionResult.failed(new RuntimeException("failed")));

			sut.executionStarted(abortedContainer);
			sut.executionFinished(abortedContainer, TestExecutionResult.aborted(new RuntimeException("aborted")));

			sut.executionStarted(abortedTest);
			sut.executionFinished(abortedTest, TestExecutionResult.aborted(new RuntimeException("aborted")));

			sut.testPlanExecutionFinished(testPlan);

			// Verify result
			IssueReport report = sut.issueReport;

			assertThat(report.getIssues()).isNotNull();
			assertThat(report.getIssues().getIssue()).isNotNull();

			// Verify issues
			List<Issue> allIssues = report.getIssues().getIssue();
			assertThat(allIssues.size()).isEqualTo(1);

			Stream<String> acutalIssuesIds = allIssues.stream().map(Issue::getIssueId);
			assertThat(acutalIssuesIds).containsExactlyInAnyOrder("succTest");

			// Verfiy published issue
			Issue succTest = allIssues.get(0);

			assertThat(succTest.getTestCases()).isNotNull();
			assertThat(succTest.getTestCases().getTestCase()).isNotNull();

			List<TestCase> succTestTestCases = succTest.getTestCases().getTestCase();
			assertThat(succTestTestCases.size()).isEqualTo(1);
			TestCase succTestTestCase = succTestTestCases.get(0);
			assertThat(succTestTestCase.getName()).isEqualTo("[test:t1]");
			assertThat(succTestTestCase.getStatus()).isEqualTo("SUCCESSFUL");

		}

	}

	@SuppressWarnings("deprecation")
	private TestIdentifier createTestIdentifier(String uniqueId) {
		TestIdentifier identifier = TestIdentifier
				.from(new TestDescriptorStub(UniqueId.root("test", uniqueId), uniqueId));
		testPlan.add(identifier);
		return identifier;
	}

	@SuppressWarnings("deprecation")
	private TestIdentifier createTestIdentifier(String uniqueId, TestIdentifier parent) {
		TestIdentifier identifier = TestIdentifier
				.from(new TestDescriptorStub(UniqueId.root("test", uniqueId), uniqueId, parent));
		testPlan.add(identifier);
		return identifier;
	}

	@SuppressWarnings("deprecation")
	private TestIdentifier createContainerIdentifier(String uniqueId) {
		TestIdentifier identifier = TestIdentifier
				.from(new TestDescriptorStub(UniqueId.root("container", uniqueId), uniqueId) {

					@Override
					public Type getType() {
						return Type.CONTAINER;
					}

				});
		testPlan.add(identifier);
		return identifier;
	}

	@SuppressWarnings("deprecation")
	private TestIdentifier createContainerIdentifier(String uniqueId, TestIdentifier parent) {
		TestIdentifier identifier = TestIdentifier
				.from(new TestDescriptorStub(UniqueId.root("container", uniqueId), uniqueId, parent) {

					@Override
					public Type getType() {
						return Type.CONTAINER;
					}

				});
		testPlan.add(identifier);
		return identifier;
	}

	static class TestDescriptorStub extends AbstractTestDescriptor {

		public TestDescriptorStub(UniqueId uniqueId, String displayName) {
			super(uniqueId, displayName);
		}

		public TestDescriptorStub(UniqueId uniqueId, String displayName, TestIdentifier parent) {
			super(uniqueId, displayName);
			setParent(new TestDescriptorStub(UniqueId.parse(parent.getUniqueId()), parent.getDisplayName()));
		}

		@Override
		public Type getType() {
			return getChildren().isEmpty() ? Type.TEST : Type.CONTAINER;
		}

	}

}
