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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.testkit.engine.EventConditions.finished;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.cause;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.throwable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

@DisplayName("ResourceManager extension")
// TODO: Do we need a test that checks a test case with LifeCycle.PER_METHOD? Ask maintainers.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ResourceManagerExtensionTests {

	@DisplayName("when a test class has a test method with a @New(TemporaryDirectory.class)-annotated parameter")
	@ExtendWith(ResourceManagerExtension.class)
	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class WhenTestClassHasTestMethodWithNewTemporaryDirectoryAnnotatedParameterTests {

		Path recordedPath;

		@DisplayName("then the parameter is populated with a new temporary directory")
		@Order(0)
		@Test
		void thenParameterIsPopulatedWithNewTemporaryDirectory(@New(TemporaryDirectory.class) Path tempDir) {
			assertThat(tempDir).startsWith(Paths.get(System.getProperty("java.io.tmpdir")));

			recordedPath = tempDir;
		}

		@DisplayName("then the respective temporary directory is torn down when finished")
		@Order(1)
		@Test
		void thenRespectiveTemporaryDirectoryIsTornDownWhenFinished() {
			assertThat(recordedPath).doesNotExist();
		}

		@DisplayName("then the associated directory is writeable and readable")
		@Order(0)
		@Test
		void thenAssociatedDirectoryIsWriteableAndReadable(@New(TemporaryDirectory.class) Path tempDir)
				throws Exception {
			Files.write(tempDir.resolve("file.txt"), "some random text".getBytes(UTF_8));
			assertThat(tempDir.resolve("file.txt")).usingCharset(UTF_8).hasContent("some random text");
		}

	}

	// ---

	@DisplayName("when a test class has multiple test methods with a @New(TemporaryDirectory.class)-annotated parameter")
	@ExtendWith(ResourceManagerExtension.class)
	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class WhenTestClassHasMultipleTestMethodsWithNewTemporaryDirectoryAnnotatedParameterTests {

		List<Path> recordedPaths = new ArrayList<>();

		@DisplayName("then the parameter on the first test method is populated with a new temporary directory")
		@Order(0)
		@Test
		void thenParameterOnFirstTestMethodIsPopulatedWithNewTemporaryDirectory(
				@New(TemporaryDirectory.class) Path tempDir) {
			assertThat(tempDir).startsWith(Paths.get(System.getProperty("java.io.tmpdir")));
			recordedPaths.add(tempDir);
		}

		@DisplayName("then the parameter on the second test method is populated with a new temporary directory")
		@Order(0)
		@Test
		void thenParameterOnSecondTestMethodIsPopulatedWithNewTemporaryDirectory(
				@New(TemporaryDirectory.class) Path tempDir) {
			assertThat(tempDir).startsWith(Paths.get(System.getProperty("java.io.tmpdir")));
			recordedPaths.add(tempDir);
		}

		@DisplayName("then the respective temporary directories are torn down when finished")
		@Order(1)
		@Test
		void thenRespectiveTemporaryDirectoryIsTornDownWhenFinished() {
			assertThat(recordedPaths).hasSize(2).allSatisfy(path -> assertThat(path).doesNotExist());
		}

		@DisplayName("then the respective temporary directories are distinct")
		@Order(1)
		@Test
		void thenRespectiveTemporaryDirectoriesAreDistinct() {
			assertThat(recordedPaths.stream().distinct()).hasSize(recordedPaths.size());
		}

	}

	// ---

	// TODO: Check that everything works fine when a test method has two @New-annotated parameters

	// ---

	Path recordedPathFromConstructor;

	@DisplayName("when a test class has a constructor with a @New(TemporaryDirectory.class)-annotated parameter")
	@ExtendWith(ResourceManagerExtension.class)
	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class WhenTestClassHasConstructorWithNewTemporaryDirectoryAnnotatedParameterTests {

		WhenTestClassHasConstructorWithNewTemporaryDirectoryAnnotatedParameterTests(
				@New(TemporaryDirectory.class) Path tempDir) {
			recordedPathFromConstructor = tempDir;
		}

		@DisplayName("then the parameter is populated with a new temporary directory")
		@Test
		void thenParameterIsPopulatedWithNewTemporaryDirectory() {
			assertThat(recordedPathFromConstructor).startsWith(Paths.get(System.getProperty("java.io.tmpdir")));
		}

	}

	@AfterAll
	@DisplayName("Check that the temporary directory created in a test class constructor is torn down")
	void checkThatTemporaryDirectoryCreatedInTestClassConstructorIsTornDown() {
		assertThat(recordedPathFromConstructor).doesNotExist();
	}

	// ---

	@DisplayName("when ResourceManagerExtension is applied to a test method with an unannotated parameter")
	@Nested
	class WhenResourceManagerExtensionIsAppliedToTestMethodWithUnannotatedParameterTests {

		@DisplayName("then ResourceManagerExtension does not populate the parameter")
		@Test
		void thenSupportsParameterReturnsTrue() {
			ExecutionResults executionResults = PioneerTestKit.executeTestClass(UnannotatedParameterTestCase.class);
			executionResults
					.testEvents()
					.assertThatEvents()
					.haveExactly(1,
						finished(throwable(message(
							m -> m.startsWith("No ParameterResolver registered for parameter [java.lang.Object") && m
									.endsWith("in method [void org.junitpioneer.jupiter."
											+ "ResourceManagerExtensionTests$UnannotatedParameterTestCase."
											+ "theTest(java.lang.Object)].")))));
		}

	}

	@ExtendWith(ResourceManagerExtension.class)
	static class UnannotatedParameterTestCase {

		@Test
		void theTest(Object randomParameter) {

		}

	}

	// ---

	@DisplayName("when a ResourceFactory is applied to a parameter and the factory throws on ::create")
	@Nested
	class WhenResourceFactoryAppliesToParameterAndFactoryThrowsOnCreateTests {

		@DisplayName("then the thrown exception is propagated")
		@Test
		void thenThrownExceptionIsPropagated() {
			ExecutionResults executionResults = PioneerTestKit.executeTestClass(ThrowingResourceFactoryTestCase.class);
			executionResults
					.testEvents()
					.debug()
					.assertThatEvents()
					.haveExactly(//
						1, //
						finished(//
							throwable(//
								instanceOf(ParameterResolutionException.class), //
								message(String
										.format("Unable to create an instance of `%s`", ThrowingResourceFactory.class)), //
								cause(//
									instanceOf(EXPECTED_EXCEPTION.getClass()), //
									message(EXPECTED_EXCEPTION.getMessage())))));
		}

	}

	@ExtendWith(ResourceManagerExtension.class)
	static class ThrowingResourceFactoryTestCase {

		@Test
		void foo(@New(ThrowingResourceFactory.class) Object object) {

		}

	}

	static final class ThrowingResourceFactory implements ResourceFactory<Object> {

		@Override
		public Resource<Object> create() throws Exception {
			throw EXPECTED_EXCEPTION;
		}

		@Override
		public void close() {
			// do nothing
		}

	}

	private static final Exception EXPECTED_EXCEPTION = new IOException("failed to connect to the Matrix");

	// TODO: Write and test with two custom ResourceFactory implementations: jimfs and OkHttp's MockWebServer
}
