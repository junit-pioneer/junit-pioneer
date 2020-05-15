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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

@DisplayName("PioneerReportListener ")
@Execution(ExecutionMode.SAME_THREAD)
public class PioneerReportListenerTests {

	private final PioneerReportListener listener = new PioneerReportListener();
	private final TestPlan testPlan = TestPlan.from(Collections.emptyList());

	@Nested
	@DisplayName("creates a report that ")
	class CreateReportTests {

		@DisplayName("is empty, if no container or test were executed and no entry was published")
		@Test
		void emptyReport() {
			listener.testPlanExecutionStarted(testPlan);
			listener.testPlanExecutionFinished(testPlan);

			// Verify result
			PioneerReport report = listener.pioneerReport;
			assertThat(report.getProperties()).isNull();
			assertThat(report.getTestcontainer().isEmpty()).isTrue();
			assertThat(report.getTestcase().isEmpty()).isTrue();
		}

		@DisplayName("contains test containers and tests with published entries as their properties")
		@Test
		void reportingCorrectCounts() {

			ReportEntry successfulContainerEntry = ReportEntry.from("key: succContainer", "value: succContainer");
			ReportEntry successfulTestEntry = ReportEntry.from("key: succTest", "value: succTest");

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

			listener.reportingEntryPublished(successfulContainer, successfulContainerEntry);

			listener.executionStarted(successfulTest);
			listener.executionFinished(successfulTest, TestExecutionResult.successful());

			listener.reportingEntryPublished(successfulTest, successfulTestEntry);

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
			PioneerReport report = listener.pioneerReport;
			assertThat(report.getProperties()).isNull();

			// Verify test containers

			assertThat(report.getTestcontainer().size()).isEqualTo(4);

			List<String> actualContainerNames = report
					.getTestcontainer()
					.stream()
					.map(Testcontainer::getName)
					.collect(Collectors.toList());
			assertThat(actualContainerNames).containsExactlyInAnyOrder("c1", "c2", "c3", "c4");

			Optional<Testcontainer> testcontainer = report
					.getTestcontainer()
					.stream()
					.filter(e -> e.getName().equals("c1"))
					.findFirst();
			assertThat(testcontainer.isPresent()).isTrue();
			Property testcontainerProperty = testcontainer.get().getProperties().getProperty().get(0);
			assertThat(testcontainerProperty.getName()).isEqualTo("key: succContainer");
			assertThat(testcontainerProperty.getValue()).isEqualTo("value: succContainer");

			// Verify test cases
			assertThat(report.getTestcase().size()).isEqualTo(4);

			List<String> actualTestcaseNames = report
					.getTestcase()
					.stream()
					.map(Testcase::getName)
					.collect(Collectors.toList());
			assertThat(actualTestcaseNames).containsExactlyInAnyOrder("t1", "t2", "t3", "t4");

			Optional<Testcase> testcase = report
					.getTestcase()
					.stream()
					.filter(e -> e.getName().equals("t1"))
					.findFirst();
			assertThat(testcase.isPresent()).isTrue();
			assertThat(testcase.get().getStatus()).isEqualTo("SUCCESSFUL");
			Property testcaseProperty = testcase.get().getProperties().getProperty().get(0);
			assertThat(testcaseProperty.getName()).isEqualTo("key: succTest");
			assertThat(testcaseProperty.getValue()).isEqualTo("value: succTest");
		}

	}
	@Nested
	@DisplayName("s method ")
	class ProcessMethodsTests {

		List<Property> allPropsOfT1 = Arrays.asList(new Property("issue", "REQ-123"), new Property("key", "value"));
		List<Property> allPropsOfT2 = Arrays.asList(new Property("someone", "value"), new Property("key", "value"));
		List<Property> allPropsOfC2 = Arrays.asList(new Property("someone", "value"), new Property("key", "value"));
		List<Property> allPropsOfC3 = Arrays.asList(new Property("someone", "value"), new Property("key", "value"));

		@BeforeEach
		void setUp() {
			listener.testStatusCache.clear();
			listener.propertyCache.clear();
			listener.propertyCache.put("[test:t1]", allPropsOfT1);
			listener.propertyCache.put("[test:t2]", allPropsOfT2);
			listener.propertyCache.put("[container:c2]", allPropsOfC2);
			listener.propertyCache.put("[container:c3]", allPropsOfC3);

			listener.testStatusCache.clear();
			listener.testStatusCache.put("[test:t1]", "FAILED");

			listener.storedTestPlan = testPlan;
		}

		@Nested
		@DisplayName("getProperty() returns ")
		class GetPropertyTests {

			@DisplayName(" an empty list, if no properties are stored")
			@Test
			void getPropertyReturnsEmptyListIfNoPropertiesAreStored() {
				Properties result = listener.getProperties("[test:t3]");

				assertThat(result).isNotNull();
				assertThat(result.getProperty()).isEmpty();
			}

			@DisplayName(" list with properties, matching the ones stored for the identifier")
			@Test
			void getPropertyReturnsListWithPropertiesIfPropertiesAreStored() {
				Properties result = listener.getProperties("[test:t1]");

				assertThat(result.getProperty()).containsAll(allPropsOfT1);
				assertThat(result.getProperty()).doesNotContainAnyElementsOf(allPropsOfT2);
			}

		}
		@Nested
		@DisplayName("processTest() returns Testcase ")
		class ProcessTestTests {

			@DisplayName(" with properties and status, if such are stored")
			@Test
			void processTestReturnsTestcaseWithPropertiesIfPropertiesAndStatusAreStored() {
				TestIdentifier testCaseIdentifier = createTestIdentifier("t1");

				Testcase result = listener.processTest(testCaseIdentifier);

				assertThat(result).isNotNull();
				assertThat(result.getName()).isEqualTo("t1");
				assertThat(result.getStatus()).isEqualTo("FAILED");
				assertThat(result.getProperties().getProperty()).containsAll(allPropsOfT1);
				assertThat(result.getProperties().getProperty()).doesNotContainAnyElementsOf(allPropsOfT2);
			}

			@DisplayName(" with no properties and UNKNOWN status, if nothing is stored")
			@Test
			void processTestReturnsTestcaseWithoutPropertiesAndUnknownStatusIfNothingIsStored() {
				TestIdentifier testCaseIdentifier = createTestIdentifier("t3");

				Testcase result = listener.processTest(testCaseIdentifier);

				assertThat(result).isNotNull();
				assertThat(result.getName()).isEqualTo("t3");
				assertThat(result.getStatus()).isEqualTo("UNKNOWN");
				assertThat(result.getProperties()).isNull();
			}

		}

		@Nested
		@DisplayName("processContainer() returns ")
		class ProcessContainerTests {

			@DisplayName(" with no properties, Testcontainer and Testcases, if nothing is stored")
			@Test
			void processContainerReturnsTestcontainerWithoutPropertiesContainersAndTestcasesIfNothingIsStored() {
				TestIdentifier testContainerIdentifier = createContainerIdentifier("c1");

				Testcontainer result = listener.processContainer(testContainerIdentifier);

				assertThat(result).isNotNull();
				assertThat(result.getName()).isEqualTo("c1");
				assertThat(result.getProperties()).isNull();
				assertThat(result.getTestcase()).isEmpty();
				assertThat(result.getTestcontainer()).isEmpty();
			}

			@DisplayName(" with properties, Testcontainer and Testcases, if stored")
			@Test
			void processContainerReturnsTestcontaineWithPropertiesIfStored() {
				TestIdentifier testContainerIdentifier = createContainerIdentifier("c2");
				TestIdentifier testContainer1 = createContainerIdentifier("c1", testContainerIdentifier);
				TestIdentifier testContainer3 = createContainerIdentifier("c3", testContainer1);

				TestIdentifier testCase1 = createTestIdentifier("t1", testContainer1);
				TestIdentifier testCase2 = createTestIdentifier("t2", testContainerIdentifier);

				Testcontainer result = listener.processContainer(testContainerIdentifier);

				// verify root level
				assertThat(result).isNotNull();
				assertThat(result.getName()).isEqualTo("c2");
				assertThat(result.getProperties().getProperty()).containsAll(allPropsOfC2);
				assertThat(result.getProperties().getProperty()).doesNotContainAnyElementsOf(allPropsOfC3);
				assertThat(result.getTestcase().size()).isEqualTo(1);
				assertThat(result.getTestcase().get(0).getName()).isEqualTo(testCase2.getDisplayName());
				assertThat(result.getTestcontainer().size()).isEqualTo(1);
				assertThat(result.getTestcontainer().get(0).getName()).isEqualTo(testContainer1.getDisplayName());

				// Verify first child container
				Testcontainer container1 = result.getTestcontainer().get(0);
				assertThat(container1.getTestcase().size()).isEqualTo(1);
				assertThat(container1.getTestcase().get(0).getName()).isEqualTo(testCase1.getDisplayName());
				assertThat(container1.getTestcontainer().size()).isEqualTo(1);
				assertThat(container1.getTestcontainer().get(0).getName()).isEqualTo(testContainer3.getDisplayName());
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
					public TestDescriptor.Type getType() {
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
					public TestDescriptor.Type getType() {
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
		public TestDescriptor.Type getType() {
			return getChildren().isEmpty() ? Type.TEST : Type.CONTAINER;
		}

	}

}
