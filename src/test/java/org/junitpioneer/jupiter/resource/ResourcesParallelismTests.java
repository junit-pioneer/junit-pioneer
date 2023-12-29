/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.resource;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;
import static org.junitpioneer.jupiter.resource.Shared.Scope.GLOBAL;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

class ResourcesParallelismTests {

	/*
	 * Asserts that parallel tests don't concurrently access shared resources.
	 * See `ResourceExtension::runSequentially` for details.
	 */

	@DisplayName("when a number of shared resources are used in the same test suite")
	@Nested
	class WhenANumberOfSharedResourcesAreUsedInSameTestSuiteTests {

		@DisplayName("then the tests do not run in parallel")
		@Execution(SAME_THREAD)
		@Test
		void thenTestsDoNotRunInParallel() {
			ExecutionResults executionResults = assertTimeoutPreemptively(Duration.ofSeconds(15),
				() -> PioneerTestKit.executeTestClass(ThrowIfTestsRunInParallelTestCases.class),
				"The tests in ThrowIfTestsRunInParallelTestCases became deadlocked!");
			assertThat(executionResults).hasNumberOfSucceededTests(3);
		}

		@DisplayName("then the test factories do not run in parallel")
		@Execution(SAME_THREAD)
		@Test
		void thenTestFactoriesDoNotRunInParallel() {
			ExecutionResults executionResults = assertTimeoutPreemptively(Duration.ofSeconds(15),
				() -> PioneerTestKit.executeTestClass(ThrowIfTestFactoriesRunInParallelTestCases.class),
				"The tests in ThrowIfTestFactoriesRunInParallelTestCases became deadlocked!");
			assertThat(executionResults).hasNumberOfSucceededTests(9);
		}

		@DisplayName("then the test templates do not run in parallel")
		@Execution(SAME_THREAD)
		@Test
		void thenTestTemplatesDoNotRunInParallel() {
			ExecutionResults executionResults = assertTimeoutPreemptively(Duration.ofSeconds(15),
				() -> PioneerTestKit.executeTestClass(ThrowIfTestTemplatesRunInParallelTestCases.class),
				"The tests in ThrowIfTestTemplatesRunInParallelTestCases became deadlocked!");
			assertThat(executionResults).hasNumberOfSucceededTests(9);
		}

		@DisplayName("then the test class constructors do not run in parallel")
		@Execution(SAME_THREAD)
		@Test
		void thenTestClassConstructorsDoNotRunInParallel() {
			ExecutionResults executionResults = assertTimeoutPreemptively(Duration.ofSeconds(15),
				() -> PioneerTestKit
						.executeTestClasses(asList(ThrowIfTestClassConstructorsRunInParallelTestCases1.class,
							ThrowIfTestClassConstructorsRunInParallelTestCases2.class,
							ThrowIfTestClassConstructorsRunInParallelTestCases3.class)),
				"The tests in ThrowIfTestTemplatesRunInParallelTestCases(1|2|3) became deadlocked!");
			assertThat(executionResults).hasNumberOfSucceededTests(3);
		}

		@DisplayName("then the @BeforeEach methods do not run in parallel")
		@Execution(SAME_THREAD)
		@Test
		void thenBeforeEachMethodsDoNotRunInParallel() {
			ExecutionResults executionResults = assertTimeoutPreemptively(Duration.ofSeconds(15),
				() -> PioneerTestKit.executeTestClass(ThrowIfBeforeEachMethodsRunInParallelTestCases.class),
				"The tests in ThrowIfBeforeEachMethodsRunInParallelTestCases became deadlocked!");
			assertThat(executionResults).hasNumberOfSucceededTests(3);
		}

		@DisplayName("then the @AfterEach methods do not run in parallel")
		@Execution(SAME_THREAD)
		@Test
		void thenAfterEachMethodsDoNotRunInParallel() {
			ExecutionResults executionResults = assertTimeoutPreemptively(Duration.ofSeconds(15),
				() -> PioneerTestKit.executeTestClass(ThrowIfAfterEachMethodsRunInParallelTestCases.class),
				"The tests in ThrowIfAfterEachMethodsRunInParallelTestCases became deadlocked!");
			assertThat(executionResults).hasNumberOfSucceededTests(3);
		}

		@DisplayName("then the @BeforeAll methods do not run in parallel")
		@Execution(SAME_THREAD)
		@Test
		void thenBeforeAllMethodsDoNotRunInParallel() {
			ExecutionResults executionResults = assertTimeoutPreemptively(Duration.ofSeconds(15_000),
				() -> PioneerTestKit.executeTestClass(ThrowIfBeforeAllMethodsRunInParallelTestCases.class),
				"The tests in ThrowIfBeforeAllMethodsRunInParallelTestCases became deadlocked!");
			assertThat(executionResults).hasNumberOfSucceededTests(3);
		}

		@DisplayName("then the @AfterAll methods do not run in parallel")
		@Execution(SAME_THREAD)
		@Test
		void thenAfterAllMethodsDoNotRunInParallel() {
			ExecutionResults executionResults = assertTimeoutPreemptively(Duration.ofSeconds(15),
				() -> PioneerTestKit.executeTestClass(ThrowIfAfterAllMethodsRunInParallelTestCases.class),
				"The tests in ThrowIfAfterAllMethodsRunInParallelTestCases became deadlocked!");
			assertThat(executionResults).hasNumberOfSucceededTests(3);
		}

	}

	private static final AtomicInteger COUNTER = new AtomicInteger(0);
	private static final int TIMEOUT_MILLIS = 20;
	private static final String SHARED_RESOURCE_A_NAME = "shared-resource-a";
	private static final String SHARED_RESOURCE_B_NAME = "shared-resource-b";
	private static final String SHARED_RESOURCE_C_NAME = "shared-resource-c";

	static class ThrowIfTestsRunInParallelTestCases {

		@Test
		void test1(
				// we don't actually use the resources, we just have them injected to verify whether sharing the
				// same resources prevent the tests from running in parallel
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_A_NAME) Path directoryA,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_B_NAME) Path directoryB)
				throws Exception {
			failIfExecutedInParallel("test1");
		}

		@Test
		void test2(
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_B_NAME) Path directoryB,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_C_NAME) Path directoryC)
				throws Exception {
			failIfExecutedInParallel("test2");
		}

		@Test
		void test3(
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_C_NAME) Path directoryC,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_A_NAME) Path directoryA)
				throws Exception {
			failIfExecutedInParallel("test3");
		}

	}

	static class ThrowIfTestFactoriesRunInParallelTestCases {

		@TestFactory
		Stream<DynamicTest> test1(
				// we don't actually use the resources, we just have them injected to verify whether sharing the
				// same resources prevent the tests from running in parallel
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_A_NAME) Path directoryA,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_B_NAME) Path directoryB)
				throws Exception {
			failIfExecutedInParallel("test1");
			return DynamicTest
					.stream(Stream.of("DynamicTest1", "DynamicTest2", "DynamicTest3"), name -> "test1" + name,
						ResourcesParallelismTests::failIfExecutedInParallel);
		}

		@TestFactory
		Stream<DynamicTest> test2(
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_B_NAME) Path directoryB,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_C_NAME) Path directoryC)
				throws Exception {
			failIfExecutedInParallel("test2");
			return DynamicTest
					.stream(Stream.of("DynamicTest1", "DynamicTest2", "DynamicTest3"), name -> "test2" + name,
						ResourcesParallelismTests::failIfExecutedInParallel);
		}

		@TestFactory
		Stream<DynamicTest> test3(
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_C_NAME) Path directoryC,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_A_NAME) Path directoryA)
				throws Exception {
			failIfExecutedInParallel("test3");
			return DynamicTest
					.stream(Stream.of("DynamicTest1", "DynamicTest2", "DynamicTest3"), name -> "test3" + name,
						ResourcesParallelismTests::failIfExecutedInParallel);
		}

	}

	static class ThrowIfTestTemplatesRunInParallelTestCases {

		@ParameterizedTest
		@ValueSource(ints = { 1, 2, 3 })
		void test1(@SuppressWarnings("unused") int iteration,
				// we don't actually use the resources, we just have them injected to verify whether sharing the
				// same resources prevent the tests from running in parallel
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_A_NAME) Path directoryA,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_B_NAME) Path directoryB)
				throws Exception {
			failIfExecutedInParallel("test1Iteration" + iteration);
		}

		@ParameterizedTest
		@ValueSource(ints = { 1, 2, 3 })
		void test2(@SuppressWarnings("unused") int iteration,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_B_NAME) Path directoryB,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_C_NAME) Path directoryC)
				throws Exception {
			failIfExecutedInParallel("test2Iteration" + iteration);
		}

		@ParameterizedTest
		@ValueSource(ints = { 1, 2, 3 })
		void test3(@SuppressWarnings("unused") int iteration,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_C_NAME) Path directoryC,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_A_NAME) Path directoryA)
				throws Exception {
			failIfExecutedInParallel("test3Iteration" + iteration);
		}

	}

	static class ThrowIfTestClassConstructorsRunInParallelTestCases1 {

		ThrowIfTestClassConstructorsRunInParallelTestCases1(
				// we don't actually use the resources, we just have them injected to verify whether sharing the
				// same resources prevent the test constructors from running in parallel
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_A_NAME, scope = GLOBAL) Path directoryA,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_B_NAME, scope = GLOBAL) Path directoryB)
				throws Exception {
			failIfExecutedInParallel("testConstructor1");
		}

		@Test
		void fakeTest() {
		}

	}

	static class ThrowIfTestClassConstructorsRunInParallelTestCases2 {

		ThrowIfTestClassConstructorsRunInParallelTestCases2(
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_B_NAME, scope = GLOBAL) Path directoryB,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_C_NAME, scope = GLOBAL) Path directoryC)
				throws Exception {
			failIfExecutedInParallel("testConstructor2");
		}

		@Test
		void fakeTest() {
		}

	}

	static class ThrowIfTestClassConstructorsRunInParallelTestCases3 {

		ThrowIfTestClassConstructorsRunInParallelTestCases3(
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_C_NAME, scope = GLOBAL) Path directoryC,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_A_NAME, scope = GLOBAL) Path directoryA)
				throws Exception {
			failIfExecutedInParallel("testConstructor3");
		}

		@Test
		void fakeTest() {
		}

	}

	static class ThrowIfBeforeEachMethodsRunInParallelTestCases {

		@BeforeEach
		void setup1(TestInfo testInfo,
				// we don't actually use the resources, we just have them injected to verify whether sharing the
				// same resources prevents the @BeforeEach methods from running multiple times in parallel
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_A_NAME) Path directoryA,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_B_NAME) Path directoryB)
				throws InterruptedException {
			failIfExecutedInParallel("testBeforeEach1-" + testInfo.getTestMethod().get().getName());
		}

		@BeforeEach
		void setup2(TestInfo testInfo,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_B_NAME) Path directoryB,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_C_NAME) Path directoryC)
				throws InterruptedException {
			failIfExecutedInParallel("testBeforeEach2-" + testInfo.getTestMethod().get().getName());
		}

		@BeforeEach
		void setup3(TestInfo testInfo,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_C_NAME) Path directoryC,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_A_NAME) Path directoryA)
				throws InterruptedException {
			failIfExecutedInParallel("testBeforeEach3-" + testInfo.getTestMethod().get().getName());
		}

		@Test
		void fakeTest1() {
		}

		@Test
		void fakeTest2() {
		}

		@Test
		void fakeTest3() {
		}

	}

	static class ThrowIfAfterEachMethodsRunInParallelTestCases {

		@Test
		void fakeTest1() {
		}

		@Test
		void fakeTest2() {
		}

		@Test
		void fakeTest3() {
		}

		@AfterEach
		void teardown1(TestInfo testInfo,
				// we don't actually use the resources, we just have them injected to verify whether sharing the
				// same resources prevents the @AfterEach methods from running multiple times in parallel
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_A_NAME) Path directoryA,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_B_NAME) Path directoryB)
				throws InterruptedException {
			failIfExecutedInParallel("testAfterEach1-" + testInfo.getTestMethod().get().getName());
		}

		@AfterEach
		void teardown2(TestInfo testInfo,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_B_NAME) Path directoryB,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_C_NAME) Path directoryC)
				throws InterruptedException {
			failIfExecutedInParallel("testAfterEach2-" + testInfo.getTestMethod().get().getName());
		}

		@AfterEach
		void teardown3(TestInfo testInfo,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_C_NAME) Path directoryC,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_A_NAME) Path directoryA)
				throws InterruptedException {
			failIfExecutedInParallel("testAfterEach3-" + testInfo.getTestMethod().get().getName());
		}

	}

	static class ThrowIfBeforeAllMethodsRunInParallelTestCases {

		@BeforeAll
		static void setup(
				// we don't actually use the resources, we just have them injected to verify whether sharing the
				// same resources prevents the @BeforeAll methods from running multiple times in parallel
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_A_NAME) Path directoryA,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_B_NAME) Path directoryB)
				throws InterruptedException {
			failIfExecutedInParallel("testBeforeAll1");
		}

		@Test
		void fakeTest() {
		}

		@Nested
		@TestInstance(TestInstance.Lifecycle.PER_CLASS)
		class NestedTestCases1 {

			@BeforeAll
			void setup(
					@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_B_NAME) Path directoryB,
					@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_C_NAME) Path directoryC)
					throws InterruptedException {
				failIfExecutedInParallel("testBeforeAll2");
			}

			@Test
			void fakeTest() {
			}

			@Nested
			@TestInstance(TestInstance.Lifecycle.PER_CLASS)
			class NestedTestCases2 {

				@BeforeAll
				void setup(
						@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_C_NAME) Path directoryC,
						@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_A_NAME) Path directoryA)
						throws InterruptedException {
					failIfExecutedInParallel("testBeforeAll3");
				}

				@Test
				void fakeTest() {
				}

			}

		}

	}

	static class ThrowIfAfterAllMethodsRunInParallelTestCases {

		@Test
		void fakeTest() {
		}

		@AfterAll
		static void teardown(
				// we don't actually use the resources, we just have them injected to verify whether sharing the
				// same resources prevents the @AfterAll methods from running multiple times in parallel
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_A_NAME) Path directoryA,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_B_NAME) Path directoryB)
				throws InterruptedException {
			failIfExecutedInParallel("testAfterAll1");
		}

		@Nested
		@TestInstance(TestInstance.Lifecycle.PER_CLASS)
		class NestedTestCases1 {

			@Test
			void fakeTest() {
			}

			@AfterAll
			void teardown(
					@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_B_NAME) Path directoryB,
					@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_C_NAME) Path directoryC)
					throws InterruptedException {
				failIfExecutedInParallel("testAfterAll2");
			}

			@Nested
			@TestInstance(TestInstance.Lifecycle.PER_CLASS)
			class NestedTestCases2 {

				@Test
				void fakeTest() {
				}

				@AfterAll
				void teardown(
						@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_C_NAME) Path directoryC,
						@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_A_NAME) Path directoryA)
						throws InterruptedException {
					failIfExecutedInParallel("testAfterAll3");
				}

			}

		}

	}

	// this method is written to fail if it is executed at overlapping times in different threads
	private static void failIfExecutedInParallel(String testName) throws InterruptedException {
		try {
			System.out.println(Thread.currentThread() + ": " + testName + ": COUNTER = " + COUNTER);
			boolean wasZero = COUNTER.compareAndSet(0, 1);
			assertThat(wasZero).isTrue();
			// wait for the next test to catch up and potentially fail
			Thread.sleep(TIMEOUT_MILLIS);
			System.out.println(Thread.currentThread() + ": " + testName + ": COUNTER = " + COUNTER);
			boolean wasOne = COUNTER.compareAndSet(1, 2);
			assertThat(wasOne).isTrue();
			// wait for the last test to catch up and potentially fail
			Thread.sleep(TIMEOUT_MILLIS);
			System.out.println(Thread.currentThread() + ": " + testName + ": COUNTER = " + COUNTER);
			boolean wasTwo = COUNTER.compareAndSet(2, 0);
			assertThat(wasTwo).isTrue();
		}
		catch (AssertionError e) {
			System.out.println(Thread.currentThread() + ": " + testName + ": e = " + stackTraceToString(e));
			throw e;
		}
	}

	private static String stackTraceToString(Throwable throwable) {
		StringWriter writer = new StringWriter();
		throwable.printStackTrace(new PrintWriter(writer));
		return writer.toString();
	}

}
