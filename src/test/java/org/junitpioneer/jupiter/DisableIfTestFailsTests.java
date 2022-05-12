/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;
import static org.junitpioneer.testkit.PioneerTestKit.abort;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.parallel.Execution;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

class DisableIfTestFailsTests {

	@Test
	void threeTestsWithSecondFailing_thirdIsDisabled() {
		ExecutionResults results = PioneerTestKit.executeTestClass(ThreeTestsWithSecondFailingTestCase.class);

		assertThat(results)
				.hasNumberOfStartedTests(2)
				.hasNumberOfSucceededTests(1)
				.hasNumberOfFailedTests(1)
				.hasNumberOfSkippedTests(1);
	}

	@Test
	void threeTestsWithSecondFailingWithUnconfiguredAssertion_thirdIsEnabled() {
		ExecutionResults results = PioneerTestKit
				.executeTestClass(ThreeTestsWithSecondFailingWithUnconfiguredAssertionTestCase.class);

		assertThat(results)
				.hasNumberOfStartedTests(3)
				.hasNumberOfSucceededTests(2)
				.hasNumberOfFailedTests(1)
				.hasNumberOfSkippedTests(0);
	}

	@Test
	void threeTestsWithSecondFailingWithConfiguredException_thirdIsDisabled() {
		ExecutionResults results = PioneerTestKit
				.executeTestClass(ThreeTestsWithSecondThrowingConfiguredExceptionTestCase.class);

		assertThat(results)
				.hasNumberOfStartedTests(2)
				.hasNumberOfSucceededTests(1)
				.hasNumberOfFailedTests(1)
				.hasNumberOfSkippedTests(1);
	}

	@Test
	void threeTestsWithSecondFailingWithUnconfiguredException_thirdIsDisabled() {
		ExecutionResults results = PioneerTestKit
				.executeTestClass(ThreeTestsWithSecondThrowingUnconfiguredExceptionTestCase.class);

		assertThat(results).hasNumberOfStartedTests(3).hasNumberOfSucceededTests(2).hasNumberOfFailedTests(1);
	}

	@Test
	void threeTestsWithSecondFailingWithAssumption_thirdIsEnabled() {
		ExecutionResults results = PioneerTestKit
				.executeTestClass(ThreeTestsWithSecondFailingWithAssumptionTestCase.class);

		assertThat(results).hasNumberOfStartedTests(3).hasNumberOfSucceededTests(2).hasNumberOfAbortedTests(1);
	}

	@Test
	void annotationOnOuterClass_innerTestFails_innerTestsDisabled() {
		ExecutionResults results = PioneerTestKit.executeTestClass(InnerTestsFailTestCase.class);

		// these assertions depend on tests in outer classes getting executed first
		assertThat(results)
				.hasNumberOfStartedTests(2)
				.hasNumberOfSucceededTests(1)
				.hasNumberOfFailedTests(1)
				.hasNumberOfSkippedTests(1);
		assertThat(results).hasNumberOfStartedContainers(3);
	}

	@Test
	void annotationOnInnerAndOuterClass_innerTestFails_remainingInnerTestsDisabled() {
		ExecutionResults results = PioneerTestKit
				.executeTestClass(InnerTestsFailOtherInnerTestsGetDisabledTestCase.class);

		assertThat(results).hasNumberOfStartedTests(1).hasNumberOfFailedTests(1);
		assertThat(results).hasNumberOfStartedContainers(3).hasNumberOfSkippedContainers(1);
	}

	@Test
	void annotationOnOuterClass_outerTestFails_innerTestContainerDisabled() {
		ExecutionResults results = PioneerTestKit.executeTestClass(OuterTestsFailTestCase.class);

		// these assertions depend on tests in outer classes getting executed first
		assertThat(results).hasNumberOfStartedTests(1).hasNumberOfFailedTests(1);
		assertThat(results).hasNumberOfStartedContainers(2).hasNumberOfSkippedContainers(1);
	}

	@Test
	void annotationOnBothClasses_outerAndInnerTestsFail() {
		ExecutionResults results = PioneerTestKit.executeTestClass(OuterAndInnerTestsFailTestCase.class);

		// these assertions depend on tests in outer classes getting executed first
		assertThat(results)
				.hasNumberOfStartedTests(3)
				// one test in inner and outer each fail
				.hasNumberOfFailedTests(2)
				// outer failing test throws unrelated exception, so second outer tests runs
				.hasNumberOfSucceededTests(1)
				// inner failing test throws outer class' exception, so second inner tests is disabled
				.hasNumberOfSkippedTests(1);
		assertThat(results).hasNumberOfStartedContainers(3);
	}

	@Test
	void threeTestsPassing_noneAreDisabled() {
		ExecutionResults results = PioneerTestKit.executeTestClass(ThreeTestsPassTestCase.class);

		assertThat(results).hasNumberOfStartedTests(3).hasNumberOfSucceededTests(3);
	}

	// TEST CASES -------------------------------------------------------------------

	// The tests that run the following classes assert the extension works by counting the number of
	// executed and skipped tests, which depends on the assumption that they're executed one after another.
	// (If they're executed in parallel, all tests might already have passed the condition evaluation
	// extension point before the first test fails and so none would be disabled.)
	// The `@TestMethodOrder(MethodOrderer.OrderAnnotation.class)` creates this consecutive execution.

	@DisableIfTestFails
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	static class ThreeTestsWithSecondFailingTestCase {

		@Test
		@Order(1)
		void test1() {
		}

		@Test
		@Order(2)
		void test2() {
			fail();
		}

		@Test
		@Order(3)
		void test3() {
		}

	}

	@DisableIfTestFails(onAssertion = false)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	static class ThreeTestsWithSecondFailingWithUnconfiguredAssertionTestCase {

		@Test
		@Order(1)
		void test1() {
		}

		@Test
		@Order(2)
		void test2() {
			// fail test with assertion
			fail();
		}

		@Test
		@Order(3)
		void test3() {
		}

	}

	@DisableIfTestFails(with = IOException.class)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	static class ThreeTestsWithSecondThrowingConfiguredExceptionTestCase {

		@Test
		@Order(1)
		void test1() {
		}

		@Test
		@Order(2)
		void test2() throws IOException {
			throw new IOException();
		}

		@Test
		@Order(3)
		void test3() {
		}

	}

	@DisableIfTestFails(with = IOException.class)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	static class ThreeTestsWithSecondThrowingUnconfiguredExceptionTestCase {

		@Test
		@Order(1)
		void test1() {
		}

		@Test
		@Order(2)
		void test2() throws InterruptedException {
			throw new InterruptedException();
		}

		@Test
		@Order(3)
		void test3() {
		}

	}

	@DisableIfTestFails(onAssertion = false)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	static class ThreeTestsWithSecondFailingWithAssumptionTestCase {

		@Test
		@Order(1)
		void test1() {
		}

		@Test
		@Order(2)
		void test2() {
			abort();
		}

		@Test
		@Order(3)
		void test3() {
		}

	}

	@DisableIfTestFails(with = IOException.class)
	@Execution(SAME_THREAD)
	static class InnerTestsFailTestCase {

		@Test
		void test1() {
		}

		@Nested
		@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
		class FirstTestFailsTestCase {

			@Test
			@Order(1)
			void test1() throws IOException {
				throw new IOException();
			}

			@Test
			@Order(2)
			void test2() {
			}

		}

	}

	@DisableIfTestFails
	@Execution(SAME_THREAD)
	static class InnerTestsFailOtherInnerTestsGetDisabledTestCase {

		@DisableIfTestFails
		@Nested
		class SomeTestFailsTestCase {

			@Test
			void test() throws Exception {
				throw new Exception();
			}

		}

		@Nested
		@DisableIfTestFails
		class AnotherTestFailsTestCase {

			@Test
			void test() throws Exception {
				throw new Exception();
			}

		}

	}

	@DisableIfTestFails(with = IOException.class)
	@Execution(SAME_THREAD)
	static class OuterTestsFailTestCase {

		@Test
		void test1() throws IOException {
			throw new IOException();
		}

		@Nested
		class OneTestPassesTestCase {

			@Test
			void test1() {
			}

		}

	}

	@DisableIfTestFails(with = IOException.class)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	static class OuterAndInnerTestsFailTestCase {

		@Test
		@Order(1)
		void test1() throws InterruptedException {
			throw new InterruptedException();
		}

		@Test
		@Order(2)
		void test2() {
		}

		@Nested
		@DisableIfTestFails(with = InterruptedException.class)
		@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
		class ThreeTestsWithSecondFailingTestCase {

			@Test
			@Order(1)
			void test1() throws IOException {
				throw new IOException();
			}

			@Test
			@Order(2)
			void test2() {
			}

		}

	}

	@DisableIfTestFails(with = IOException.class)
	static class ThreeTestsPassTestCase {

		@Test
		void test1() {
		}

		@Test
		void test2() {
		}

		@Test
		void test3() {
		}

	}

}
