/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

public class DisableIfTestFailsTests {

	@Test
	void threeTestsWithSecondFailing_thirdIsDisabled() {
		ExecutionResults results = PioneerTestKit.executeTestClass(ThreeTestsWithSecondFailing.class);

		// TODO assert that failed test ran after successful test
		assertThat(results)
				.hasNumberOfStartedTests(2)
				.hasNumberOfSucceededTests(1)
				.hasNumberOfFailedTests(1)
				.hasNumberOfSkippedTests(1);
	}

	@Test
	void annotationOnOuterClass_innerTestFails_innerTestsDisabled() {
		ExecutionResults results = PioneerTestKit.executeTestClass(InnerTestsFail.class);

		// these assertions depend on tests in outer classes getting executed first
		assertThat(results)
				.hasNumberOfStartedTests(2)
				.hasNumberOfSucceededTests(1)
				.hasNumberOfFailedTests(1)
				.hasNumberOfSkippedTests(1);
		assertThat(results).hasNumberOfStartedContainers(3);
	}

	@Test
	void annotationOnOuterClass_outerTestFails_innerTestContainerDisabled() {
		ExecutionResults results = PioneerTestKit.executeTestClass(OuterTestsFail.class);

		// these assertions depend on tests in outer classes getting executed first
		assertThat(results).hasNumberOfStartedTests(1).hasNumberOfFailedTests(1);
		assertThat(results).hasNumberOfStartedContainers(2).hasNumberOfSkippedContainers(1);
	}

	@Test
	void annotationOnBothClasses_outerAndInnerTestsFail() {
		ExecutionResults results = PioneerTestKit.executeTestClass(OuterAndInnerTestsFail.class);

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
		ExecutionResults results = PioneerTestKit.executeTestClass(ThreeTestsPass.class);

		assertThat(results).hasNumberOfStartedTests(3).hasNumberOfSucceededTests(3);
	}

	// TEST CASES -------------------------------------------------------------------

	// Some tests require state to keep track of the number of test executions.
	// Storing that state in a static field keeps it around from one test suite execution to the next
	// if they are run in the same JVM (as IntelliJ does), which breaks the test.
	// One fix would be a @BeforeAll setup that resets the counter to zero, but for no apparent reason,
	// this lead to flaky tests under threading. Using a `PER_CLASS` lifecycle allows us to make it an
	// instance field and that worked.

	// The tests that run the following classes assert the extension works by counting the number of
	// executed and skipped tests, which depends on the assumption that they're executed one after another.
	// (If they're executed in parallel, all tests might already have passed the condition evaluation
	// extension point before the first test fails and so none would be disabled.)
	// Hence use `@Execution(SAME_THREAD)`.

	@TestInstance(PER_CLASS)
	@DisableIfTestFails(with = IOException.class)
	@Execution(SAME_THREAD)
	static class ThreeTestsWithSecondFailing {

		private int executionCount;

		@Test
		void test1() throws IOException {
			executionCount++;
			if (executionCount == 2)
				throw new IOException();
		}

		@Test
		void test2() throws IOException {
			executionCount++;
			if (executionCount == 2)
				throw new IOException();
		}

		@Test
		void test3() throws IOException {
			executionCount++;
			if (executionCount == 2)
				throw new IOException();
		}

	}

	@DisableIfTestFails(with = IOException.class)
	@Execution(SAME_THREAD)
	static class InnerTestsFail {

		@Test
		void test1() {
		}

		@Nested
		@TestInstance(PER_CLASS)
		class FirstTestFails {

			private int executionCount;

			@Test
			void test1() throws IOException {
				executionCount++;
				if (executionCount == 1)
					throw new IOException();
			}

			@Test
			void test2() throws IOException {
				executionCount++;
				if (executionCount == 1)
					throw new IOException();
			}

		}

	}

	@DisableIfTestFails(with = IOException.class)
	@Execution(SAME_THREAD)
	static class OuterTestsFail {

		@Test
		void test1() throws IOException {
			throw new IOException();
		}

		@Nested
		class OneTestPasses {

			@Test
			void test1() {
			}

		}

	}

	@DisableIfTestFails(with = IOException.class)
	@Execution(SAME_THREAD)
	@TestInstance(PER_CLASS)
	static class OuterAndInnerTestsFail {

		private int executionCount;

		@Test
		void test1() throws InterruptedException {
			executionCount++;
			if (executionCount == 1)
				throw new InterruptedException();
		}

		@Test
		void test2() throws InterruptedException {
			executionCount++;
			if (executionCount == 1)
				throw new InterruptedException();
		}

		@Nested
		@DisableIfTestFails(with = InterruptedException.class)
		@TestInstance(PER_CLASS)
		class ThreeTestsWithSecondFailing {

			private int executionCount;

			@Test
			void test1() throws IOException {
				executionCount++;
				if (executionCount == 1)
					throw new IOException();
			}

			@Test
			void test2() throws IOException {
				executionCount++;
				if (executionCount == 1)
					throw new IOException();
			}

		}

	}

	@DisableIfTestFails(with = IOException.class)
	static class ThreeTestsPass {

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
