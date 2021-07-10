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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

@DisplayName("ResourceManager extension")
// TODO: Do we need this annotation anymore?
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ResourceManagerExtensionTests {

	@DisplayName("when a test class has a test method with a @New(TemporaryDirectory.class)-annotated parameter")
	@Nested
	class WhenTestClassHasTestMethodWithNewTempDirParameterTests {

		@DisplayName("then the parameter is populated with a new readable and writeable temporary directory "
				+ "that lasts as long as the test")
		@Test
		void thenParameterIsPopulatedWithNewReadableAndWriteableTempDirThatLastsAsLongAsTheTest() {
			ExecutionResults executionResults = PioneerTestKit
					.executeTestClass(SingleTestMethodWithNewTempDirParameterTestCase.class);
			executionResults.testEvents().debug().succeeded().assertThatEvents().hasSize(1);
			assertThat(SingleTestMethodWithNewTempDirParameterTestCase.recordedPath).doesNotExist();
		}

	}

	@ExtendWith(ResourceManagerExtension.class)
	static class SingleTestMethodWithNewTempDirParameterTestCase {

		static Path recordedPath;

		@Test
		void theTest(@New(TemporaryDirectory.class) Path tempDir) throws Exception {
			assertEmptyReadableWriteableTemporaryDirectory(tempDir);

			recordedPath = tempDir;
		}

	}

	// ---

	@DisplayName("when a test class has multiple test methods with a @New(TemporaryDirectory.class)-annotated parameter")
	@Nested
	class WhenTestClassHasMultipleTestMethodsWithNewTempDirAnnotatedParameterTests {

		@DisplayName("then the parameters on both test methods are populated with new readable and writeable "
				+ "temporary directories that are torn down afterwards")
		@Test
		void thenParametersOnBothTestMethodsArePopulatedWithNewReadableAndWriteableTempDirsThatAreTornDownAfterwards() {
			ExecutionResults executionResults = PioneerTestKit
					.executeTestClass(TwoTestMethodsWithNewTempDirParameterTestCase.class);
			executionResults.testEvents().debug().succeeded().assertThatEvents().hasSize(2);
			assertThat(TwoTestMethodsWithNewTempDirParameterTestCase.recordedPaths)
					.hasSize(2)
					.doesNotHaveDuplicates()
					.allSatisfy(path -> assertThat(path).doesNotExist());
		}

	}

	@ExtendWith(ResourceManagerExtension.class)
	static class TwoTestMethodsWithNewTempDirParameterTestCase {

		static List<Path> recordedPaths = new CopyOnWriteArrayList<>();

		@Test
		void firstTest(@New(TemporaryDirectory.class) Path tempDir) throws Exception {
			assertEmptyReadableWriteableTemporaryDirectory(tempDir);

			recordedPaths.add(tempDir);
		}

		@Test
		void secondTest(@New(TemporaryDirectory.class) Path tempDir) throws Exception {
			assertEmptyReadableWriteableTemporaryDirectory(tempDir);

			recordedPaths.add(tempDir);
		}

	}

	// ---

	// TODO: Check that everything works fine when a test method has two @New-annotated parameters

	// ---

	@DisplayName("when a test class has a constructor with a @New(TemporaryDirectory.class)-annotated parameter")
	@Nested
	class WhenTestClassHasConstructorWithNewTemporaryDirectoryAnnotatedParameterTests {

		@DisplayName("then the parameter is populated with a new readable and writeable temporary directory "
				+ "that lasts as long as the test instance")
		@Test
		void thenParameterIsPopulatedWithNewReadableAndWriteableTempDirThatLastsAsLongAsTheTestInstance() {
			ExecutionResults executionResults = PioneerTestKit
					.executeTestClass(SingleTestConstructorWithNewTempDirParameterTestCase.class);
			executionResults.testEvents().debug().succeeded().assertThatEvents().hasSize(1);
			assertThat(SingleTestConstructorWithNewTempDirParameterTestCase.recordedPathFromConstructor).doesNotExist();
		}

	}

	@ExtendWith(ResourceManagerExtension.class)
	static class SingleTestConstructorWithNewTempDirParameterTestCase {

		static Path recordedPathFromConstructor;

		SingleTestConstructorWithNewTempDirParameterTestCase(@New(TemporaryDirectory.class) Path tempDir) {
			recordedPathFromConstructor = tempDir;
		}

		@Test
		void theTest() throws Exception {
			assertEmptyReadableWriteableTemporaryDirectory(recordedPathFromConstructor);
		}

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
					.debug()
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

	// ---

	// TODO: Write and test with two custom ResourceFactory implementations: jimfs and OkHttp's MockWebServer

	// ---

	private static void assertEmptyReadableWriteableTemporaryDirectory(@New(TemporaryDirectory.class) Path tempDir)
			throws IOException {
		assertThat(tempDir).isEmptyDirectory();
		assertThat(tempDir).startsWith(Paths.get(System.getProperty("java.io.tmpdir")));
		assertThat(tempDir).isReadable();
		assertThat(tempDir).isWritable();

		Files.write(tempDir.resolve("file.txt"), "some random text".getBytes(UTF_8));
		assertThat(tempDir.resolve("file.txt")).usingCharset(UTF_8).hasContent("some random text");
	}

}
