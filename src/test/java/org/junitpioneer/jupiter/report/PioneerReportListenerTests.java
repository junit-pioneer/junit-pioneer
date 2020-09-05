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
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
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

	private final PioneerReportListener sut = new PioneerReportListener();
	private final TestPlan testPlan = TestPlan.from(Collections.emptyList());

	@Nested
	@DisplayName("writeReport method ")
	class CreateReportTests {

		@DisplayName("throws and PioneerReportException when an exception was caught")
		@Test
		void throwRuntimeExceptionWhileCreatingReport() {
			Throwable thrown = Assertions.assertThrows(Throwable.class, () -> {
				sut.writeReport(null);
			});

			assertThat(thrown).isExactlyInstanceOf(PioneerReportException.class);
			assertThat(thrown.getMessage()).isEqualTo("Error while creating the pioneer report");
			assertThat(thrown.getCause()).isExactlyInstanceOf(IllegalArgumentException.class);

		}

		@DisplayName("creates an empty report, if no container or test were executed and no entry was published")
		@Test
		void emptyReport() {
			sut.testPlanExecutionStarted(testPlan);
			sut.testPlanExecutionFinished(testPlan);

			// Verify result
			PioneerReport report = sut.pioneerReport;
			assertThat(report.getProperties()).isNull();
			assertThat(report.getTestContainer().isEmpty()).isTrue();
			assertThat(report.getTestCase().isEmpty()).isTrue();
		}

		@DisplayName("creates a report with test containers and tests with published entries as their properties")
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

			sut.testPlanExecutionStarted(testPlan);

			sut.executionSkipped(skippedContainer, "skipped");
			sut.executionSkipped(skippedTest, "skipped");

			sut.executionStarted(successfulContainer);
			sut.executionFinished(successfulContainer, TestExecutionResult.successful());

			sut.reportingEntryPublished(successfulContainer, successfulContainerEntry);

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
			PioneerReport report = sut.pioneerReport;
			assertThat(report.getProperties()).isNull();

			// Verify test containers

			assertThat(report.getTestContainer().size()).isEqualTo(4);

			Stream<String> actualContainerNames = report.getTestContainer().stream().map(TestContainer::getName);
			assertThat(actualContainerNames).containsExactlyInAnyOrder("c1", "c2", "c3", "c4");

			Optional<TestContainer> testcontainer = report
					.getTestContainer()
					.stream()
					.filter(e -> e.getName().equals("c1"))
					.findFirst();
			assertThat(testcontainer.isPresent()).isTrue();
			Property testcontainerProperty = testcontainer.get().getProperties().getProperty().get(0);
			assertThat(testcontainerProperty.getName()).isEqualTo("key: succContainer");
			assertThat(testcontainerProperty.getValue()).isEqualTo("value: succContainer");

			// Verify test cases
			assertThat(report.getTestCase().size()).isEqualTo(4);

			Stream<String> actualTestcaseNames = report.getTestCase().stream().map(TestCase::getName);
			assertThat(actualTestcaseNames).containsExactlyInAnyOrder("t1", "t2", "t3", "t4");

			Optional<TestCase> testcase = report
					.getTestCase()
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
			sut.testStatusCache.clear();
			sut.propertyCache.clear();
			sut.propertyCache.put("[test:t1]", allPropsOfT1);
			sut.propertyCache.put("[test:t2]", allPropsOfT2);
			sut.propertyCache.put("[container:c2]", allPropsOfC2);
			sut.propertyCache.put("[container:c3]", allPropsOfC3);

			sut.testStatusCache.clear();
			sut.testStatusCache.put("[test:t1]", "FAILED");

			sut.storedTestPlan = testPlan;
		}

		@Nested
		@DisplayName("getProperty() returns ")
		class GetPropertyTests {

			@DisplayName(" an empty list, if no properties are stored")
			@Test
			void getPropertyReturnsEmptyListIfNoPropertiesAreStored() {
				Properties result = sut.getProperties("[test:t3]");

				assertThat(result).isNotNull();
				assertThat(result.getProperty()).isEmpty();
			}

			@DisplayName(" list with properties, matching the ones stored for the identifier")
			@Test
			void getPropertyReturnsListWithPropertiesIfPropertiesAreStored() {
				Properties result = sut.getProperties("[test:t1]");

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

				TestCase result = sut.processTest(testCaseIdentifier);

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

				TestCase result = sut.processTest(testCaseIdentifier);

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

				TestContainer result = sut.processContainer(testContainerIdentifier);

				assertThat(result).isNotNull();
				assertThat(result.getName()).isEqualTo("c1");
				assertThat(result.getProperties()).isNull();
				assertThat(result.getTestCase()).isEmpty();
				assertThat(result.getTestContainer()).isEmpty();
			}

			@DisplayName(" with properties, Testcontainer and Testcases, if stored")
			@Test
			void processContainerReturnsTestcontaineWithPropertiesIfStored() {
				TestIdentifier testContainerIdentifier = createContainerIdentifier("c2");
				TestIdentifier testContainer1 = createContainerIdentifier("c1", testContainerIdentifier);
				TestIdentifier testContainer3 = createContainerIdentifier("c3", testContainer1);

				TestIdentifier testCase1 = createTestIdentifier("t1", testContainer1);
				TestIdentifier testCase2 = createTestIdentifier("t2", testContainerIdentifier);

				TestContainer result = sut.processContainer(testContainerIdentifier);

				// verify root level
				assertThat(result).isNotNull();
				assertThat(result.getName()).isEqualTo("c2");
				assertThat(result.getProperties().getProperty()).containsAll(allPropsOfC2);
				assertThat(result.getProperties().getProperty()).doesNotContainAnyElementsOf(allPropsOfC3);
				assertThat(result.getTestCase().size()).isEqualTo(1);
				assertThat(result.getTestCase().get(0).getName()).isEqualTo(testCase2.getDisplayName());
				assertThat(result.getTestContainer().size()).isEqualTo(1);
				assertThat(result.getTestContainer().get(0).getName()).isEqualTo(testContainer1.getDisplayName());

				// Verify first child container
				TestContainer container1 = result.getTestContainer().get(0);
				assertThat(container1.getTestCase().size()).isEqualTo(1);
				assertThat(container1.getTestCase().get(0).getName()).isEqualTo(testCase1.getDisplayName());
				assertThat(container1.getTestContainer().size()).isEqualTo(1);
				assertThat(container1.getTestContainer().get(0).getName()).isEqualTo(testContainer3.getDisplayName());
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
