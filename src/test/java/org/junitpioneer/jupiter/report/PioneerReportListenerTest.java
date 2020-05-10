/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

@Execution(ExecutionMode.SAME_THREAD)
public class PioneerReportListenerTest {

	private final PioneerReportListener listener = new PioneerReportListener();
	private final TestPlan testPlan = TestPlan.from(Collections.emptyList());

	@Test
	void emptyReport() {
		listener.testPlanExecutionStarted(testPlan);
		listener.testPlanExecutionFinished(testPlan);

		// Verify result
		PioneerReport report = listener.getPioneerReport();
		assertThat(report.getProperties()).isNull();
		assertThat(report.getTestcontainer().isEmpty()).isTrue();
		assertThat(report.getTestcase().isEmpty()).isTrue();
	}

	@Test
	void reportingCorrectCounts() {

		ReportEntry succContainerEntry = ReportEntry.from("key: succContainer", "value: succContainer");
		ReportEntry succTestEntry = ReportEntry.from("key: succTest", "value: succTest");

		TestIdentifier successfulContainer = createContainerIdentifier("c1");
		TestIdentifier failedContainer = createContainerIdentifier("c2");
		TestIdentifier abortedContainer = createContainerIdentifier("c3");
		TestIdentifier skippedContainer = createContainerIdentifier("c4");

		TestIdentifier successfulTest = createTestIdentifier("t1");
		TestIdentifier failedTest = createTestIdentifier("t2");
		TestIdentifier abortedTest = createTestIdentifier("t3");
		TestIdentifier skippedTest = createTestIdentifier("t4");

		listener.testPlanExecutionStarted(testPlan);

		listener.executionSkipped(skippedContainer, "skipped");
		listener.executionSkipped(skippedTest, "skipped");

		listener.executionStarted(successfulContainer);
		listener.executionFinished(successfulContainer, TestExecutionResult.successful());

		listener.reportingEntryPublished(successfulContainer, succContainerEntry);

		listener.executionStarted(successfulTest);
		listener.executionFinished(successfulTest, TestExecutionResult.successful());

		listener.reportingEntryPublished(successfulTest, succTestEntry);

		listener.executionStarted(failedContainer);
		listener.executionFinished(failedContainer, TestExecutionResult.failed(new RuntimeException("failed")));

		listener.executionStarted(failedTest);
		listener.executionFinished(failedTest, TestExecutionResult.failed(new RuntimeException("failed")));

		listener.executionStarted(abortedContainer);
		listener.executionFinished(abortedContainer, TestExecutionResult.aborted(new RuntimeException("aborted")));

		listener.executionStarted(abortedTest);
		listener.executionFinished(abortedTest, TestExecutionResult.aborted(new RuntimeException("aborted")));

		listener.testPlanExecutionFinished(testPlan);

		// Verify result
		PioneerReport report = listener.getPioneerReport();
		assertThat(report.getProperties()).isNull();

		// Verify test containers
		assertThat(report.getTestcontainer().size()).isEqualTo(4);
		assertThat(report.getTestcontainer().get(0).getName()).isIn("c1", "c2", "c3", "c4");

		for (Testcontainer testcontainer : report.getTestcontainer()) {
			if (testcontainer.getName().equals("c1")) {
				Property property = testcontainer.getProperties().getProperty().get(0);
				assertThat(property.getName()).isEqualTo("key: succContainer");
				assertThat(property.getValue()).isEqualTo("value: succContainer");
			}
		}

		// Verify test cases
		assertThat(report.getTestcase().size()).isEqualTo(4);
		assertThat(report.getTestcase().get(0).getName()).isIn("t1", "t2", "t3", "t4");
		for (Testcase testcase : report.getTestcase()) {
			if (testcase.getName().equals("t1")) {
				assertThat(testcase.getStatus()).isEqualTo("SUCCESSFUL");

				Property property = testcase.getProperties().getProperty().get(0);
				assertThat(property.getName()).isEqualTo("key: succTest");
				assertThat(property.getValue()).isEqualTo("value: succTest");
			}
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
	private TestIdentifier createContainerIdentifier(String uniqueId) {
		TestIdentifier identifier = TestIdentifier
				.from(new TestDescriptorStub(UniqueId.root("container", uniqueId), uniqueId) {

					@Override
					public TestDescriptor.Type getType() {
						return Type.CONTAINER;
					}

				});
		testPlan.add(identifier);
		return identifier;
	}

	class TestDescriptorStub extends AbstractTestDescriptor {

		public TestDescriptorStub(UniqueId uniqueId, String displayName) {
			super(uniqueId, displayName);
		}

		@Override
		public TestDescriptor.Type getType() {
			return getChildren().isEmpty() ? Type.TEST : Type.CONTAINER;
		}

	}

}
