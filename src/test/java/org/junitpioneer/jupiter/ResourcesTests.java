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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.platform.testkit.engine.EventConditions.finished;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.cause;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.throwable;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

@DisplayName("Resources extension")
class ResourcesTests {

	@DisplayName("when a test class has a test method with a @New(TemporaryDirectory.class)-annotated parameter")
	@Nested
	class WhenTestClassHasTestMethodWithNewTempDirParameterTests {

		@DisplayName("then the parameter is populated with a new readable and writeable temporary directory "
				+ "that lasts as long as the test")
		@Test
		void thenParameterIsPopulatedWithNewReadableAndWriteableTempDirThatLastsAsLongAsTheTest() {
			ExecutionResults executionResults = PioneerTestKit
					.executeTestClass(SingleTestMethodWithNewTempDirParameterTestCase.class);
			assertThat(executionResults).hasSingleSucceededTest();
			assertThat(SingleTestMethodWithNewTempDirParameterTestCase.recordedPath).doesNotExist();
		}

	}

	@Resources
	static class SingleTestMethodWithNewTempDirParameterTestCase {

		static Path recordedPath;

		@Test
		void theTest(@New(TemporaryDirectory.class) Path tempDir) {
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
			assertThat(executionResults).hasNumberOfSucceededTests(2);
			assertThat(TwoTestMethodsWithNewTempDirParameterTestCase.recordedPaths)
					.hasSize(2)
					.doesNotHaveDuplicates()
					.allSatisfy(path -> assertThat(path).doesNotExist());
		}

	}

	@Resources
	static class TwoTestMethodsWithNewTempDirParameterTestCase {

		static List<Path> recordedPaths = new CopyOnWriteArrayList<>();

		@Test
		void firstTest(@New(TemporaryDirectory.class) Path tempDir) {
			assertEmptyReadableWriteableTemporaryDirectory(tempDir);

			recordedPaths.add(tempDir);
		}

		@Test
		void secondTest(@New(TemporaryDirectory.class) Path tempDir) {
			assertEmptyReadableWriteableTemporaryDirectory(tempDir);

			recordedPaths.add(tempDir);
		}

	}

	// ---

	@DisplayName("when a test class has a test method with multiple @New(TemporaryDirectory.class)-annotated parameters")
	@Nested
	class WhenTestClassHasTestMethodWithMultipleNewTempDirAnnotatedParameterTests {

		@DisplayName("then the parameters on the test method are populated with new readable and writeable "
				+ "temporary directories that are torn down afterwards")
		@Test
		void thenParametersOnTheTestMethodArePopulatedWithNewReadableAndWriteableTempDirsThatAreTornDownAfterwards() {
			ExecutionResults executionResults = PioneerTestKit
					.executeTestClass(SingleTestMethodWithTwoNewTempDirParametersTestCase.class);
			assertThat(executionResults).hasSingleSucceededTest();
			assertThat(SingleTestMethodWithTwoNewTempDirParametersTestCase.recordedPaths)
					.hasSize(2)
					.doesNotHaveDuplicates()
					.allSatisfy(path -> assertThat(path).doesNotExist());
		}

	}

	@Resources
	static class SingleTestMethodWithTwoNewTempDirParametersTestCase {

		static List<Path> recordedPaths = new CopyOnWriteArrayList<>();

		@Test
		void firstTest(@New(TemporaryDirectory.class) Path firstTempDir,
				@New(TemporaryDirectory.class) Path secondTempDir) {
			assertEmptyReadableWriteableTemporaryDirectory(firstTempDir);
			assertEmptyReadableWriteableTemporaryDirectory(secondTempDir);

			recordedPaths.addAll(asList(firstTempDir, secondTempDir));
		}

	}

	// ---

	@DisplayName("when a test class has a constructor with a @New(TemporaryDirectory.class)-annotated parameter")
	@Nested
	class WhenTestClassHasConstructorWithNewTemporaryDirectoryAnnotatedParameterTests {

		@DisplayName("then each test method has access to a new readable and writeable temporary directory "
				+ "that lasts as long as the test instance")
		@Test
		void thenEachTestMethodHasAccessToNewReadableAndWriteableTempDirThatLastsAsLongAsTestInstance() {
			ExecutionResults executionResults = PioneerTestKit
					.executeTestClass(TestConstructorWithNewTempDirParameterTestCase.class);
			assertThat(executionResults).hasNumberOfSucceededTests(2);
			assertThat(TestConstructorWithNewTempDirParameterTestCase.recordedPathsFromConstructor)
					.hasSize(2)
					.doesNotHaveDuplicates()
					.allSatisfy(path -> assertThat(path).doesNotExist());
		}

	}

	@Resources
	static class TestConstructorWithNewTempDirParameterTestCase {

		static List<Path> recordedPathsFromConstructor = new CopyOnWriteArrayList<>();
		Path recordedPath;

		TestConstructorWithNewTempDirParameterTestCase(@New(TemporaryDirectory.class) Path tempDir) {
			recordedPathsFromConstructor.add(tempDir);
			recordedPath = tempDir;
		}

		@Test
		void firstTest() {
			assertEmptyReadableWriteableTemporaryDirectory(recordedPath);
		}

		@Test
		void secondTest() {
			assertEmptyReadableWriteableTemporaryDirectory(recordedPath);
		}

	}

	// ---

	@DisplayName("when Resources is applied to a test method with an unannotated parameter")
	@Nested
	class WhenResourcesIsAppliedToTestMethodWithUnannotatedParameterTests {

		@DisplayName("then Resources does not populate the parameter")
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
											+ "ResourcesTests$UnannotatedParameterTestCase."
											+ "theTest(java.lang.Object)].")))));
		}

	}

	@Resources
	@SuppressWarnings("unused")
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
								message("Unable to create an instance of `" + ThrowingResourceFactory.class + "`"), //
								cause(//
									instanceOf(EXPECTED_EXCEPTION.getClass()), //
									message(EXPECTED_EXCEPTION.getMessage())))));
		}

	}

	@Resources
	@SuppressWarnings("unused")
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

	// TODO: It's generally good advice to avoid mocking things that we, JUnit Pioneer, don't own, because if we mock
	//       third-party things, then their real behaviour is likely to be updated over time and get out-of-sync with
	//       our mocks.
	//       Do we care about this, given that JUnit 5 avoids backwards-incompatible changes and that we're so closely
	//       tied to JUnit 5 itself?

	@DisplayName("when ResourceManagerExtension is unable to find @New on a parameter")
	@Nested
	class WhenResourceManagerExtensionUnableToFindNewOnParameter {

		@DisplayName("then an exception mentioning the parameter and the test method it's on is thrown")
		@Test
		void thenExceptionMentioningParameterAndTestMethodItsOnIsThrown() {
			ParameterContext mockParameterContext = mock(ParameterContext.class);
			when(mockParameterContext.findAnnotation(New.class)).thenReturn(Optional.empty());
			Method exampleMethod = ReflectionSupport.findMethod(String.class, "valueOf", Object.class).get();
			Parameter exampleParameter = exampleMethod.getParameters()[0];
			when(mockParameterContext.getParameter()).thenReturn(exampleParameter);
			ExtensionContext mockExtensionContext = mock(ExtensionContext.class);
			when(mockExtensionContext.getTestMethod()).thenReturn(Optional.of(exampleMethod));

			assertThatThrownBy(
				() -> new ResourceManagerExtension().resolveParameter(mockParameterContext, mockExtensionContext))
						.isInstanceOf(ParameterResolutionException.class)
						.hasMessage("Parameter `" + exampleParameter + "` on method `" + exampleMethod
								+ "` is not annotated with @New");
		}

		@DisplayName("and the test method does not exist")
		@Nested
		class AndTestMethodDoesNotExist {

			@DisplayName("then an exception mentioning just the parameter is thrown")
			@Test
			void thenExceptionMentioningJustParameterIsThrown() {
				ParameterContext mockParameterContext = mock(ParameterContext.class);
				when(mockParameterContext.findAnnotation(New.class)).thenReturn(Optional.empty());
				Method exampleMethod = ReflectionSupport.findMethod(String.class, "valueOf", Object.class).get();
				Parameter exampleParameter = exampleMethod.getParameters()[0];
				when(mockParameterContext.getParameter()).thenReturn(exampleParameter);
				ExtensionContext mockExtensionContext = mock(ExtensionContext.class);
				when(mockExtensionContext.getTestMethod()).thenReturn(Optional.empty());

				assertThatThrownBy(
					() -> new ResourceManagerExtension().resolveParameter(mockParameterContext, mockExtensionContext))
							.isInstanceOf(ParameterResolutionException.class)
							.hasMessage(
								"Parameter `" + exampleParameter + "` on unknown method is not annotated with @New");
			}

		}

	}

	// ---

	// TODO: Write and test with two custom ResourceFactory implementations: jimfs and OkHttp's MockWebServer

	// ---

	private static void assertEmptyReadableWriteableTemporaryDirectory(Path tempDir) {
		assertThat(tempDir)
				.isEmptyDirectory()
				.startsWith(Paths.get(System.getProperty("java.io.tmpdir")))
				.isReadable()
				.isWritable();
	}

}
