/*
 * Copyright 2016-2022 the original author or authors.
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
import static org.junitpioneer.jupiter.resource.Scope.GLOBAL;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

class ResourcesParallelismTests {

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

	}

	private static final AtomicInteger COUNTER = new AtomicInteger(0);
	private static final int TIMEOUT_MILLIS = 100;
	private static final String SHARED_RESOURCE_A_NAME = "shared-resource-a";
	private static final String SHARED_RESOURCE_B_NAME = "shared-resource-b";
	private static final String SHARED_RESOURCE_C_NAME = "shared-resource-c";

	static class ThrowIfTestsRunInParallelTestCases {

		// In ResourceExtension, we wrap shared resources in locks. This prevents them from being
		// used concurrently, which in turn prevents race conditions.
		//
		// However, we can still suffer from something known in computer science as the
		// "dining philosophers problem" [1].
		//
		// For example, given these tests and the respective shared resources that they want to
		// get:
		// - test1 -> [A, B]
		// - test2 -> [B, C]
		// - test3 -> [C, A]
		//
		// ...what happens if test1 gets A, then test2 gets B, then test3 gets C, then test1 tries
		// to get B?
		//
		// Well, test1 is waiting on test2 to get B, but test2 is waiting on test3 to get C, and
		// test3 is waiting on test1 to get A.
		//
		// All three tests are now waiting on each other for resources that they will never
		// release, so the tests will freeze forever!
		//
		// This is called a deadlock.
		//
		// The purpose of the tests below is to check that the tests don't run in parallel or deadlock.
		// (We're trying to check both cases at the same time.)
		//
		// [1] https://en.wikipedia.org/wiki/Dining_philosophers_problem

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

	// this method is written to fail if it is executed at overlapping times in different threads
	private static void failIfExecutedInParallel(String testName) throws InterruptedException {
		boolean wasZero = COUNTER.compareAndSet(0, 1);
		assertThat(wasZero).isTrue();
		// wait for the next test to catch up and potentially fail
		Thread.sleep(TIMEOUT_MILLIS);
		boolean wasOne = COUNTER.compareAndSet(1, 2);
		assertThat(wasOne).isTrue();
		// wait for the last test to catch up and potentially fail
		Thread.sleep(TIMEOUT_MILLIS);
		boolean wasTwo = COUNTER.compareAndSet(2, 0);
		assertThat(wasTwo).isTrue();
	}

	// At time of writing, we couldn't find a way to test that @{Before, After}{All, Each}-annotated methods are not
	// executed at overlapping times, so we do the next-best thing: we ensure that all such methods are intercepted by
	// ResourceExtension at all.

	@Test
	void checkThatBeforeAllMethodsAreIntercepted() {
		assertOverrides("interceptBeforeAllMethod");
	}

	@Test
	void checkThatAfterAllMethodsAreIntercepted() {
		assertOverrides("interceptAfterAllMethod");
	}

	@Test
	void checkThatBeforeEachMethodsAreIntercepted() {
		assertOverrides("interceptBeforeEachMethod");
	}

	@Test
	void checkThatAfterEachMethodsAreIntercepted() {
		assertOverrides("interceptAfterEachMethod");
	}

	private void assertOverrides(String methodName) {
		assertThat(InvocationInterceptor.class).isAssignableFrom(ResourceExtension.class);
		assertThat(ResourceExtension.class.getDeclaredMethods())
				.haveExactly(1, new Condition<>((Method m) -> m.getName().equals(methodName),
					"declared method with name '%s'", methodName));
	}

}
