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
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;
import static org.junit.platform.testkit.engine.EventConditions.finished;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.cause;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.throwable;
import static org.junitpioneer.internal.AllElementsAreEqual.allElementsAreEqual;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

// TODO: Some of the tests here fail when run concurrently, or repeatedly with IntelliJ's
//       "run until failure" feature:
//       https://intellij-support.jetbrains.com/hc/en-us/community/posts/206898845/comments/360000470940
//       Figure out why.

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

	static class SingleTestMethodWithNewTempDirParameterTestCase {

		static Path recordedPath;

		@Test
		void theTest(@New(TemporaryDirectory.class) Path tempDir) {
			assertEmptyReadableWriteableTemporaryDirectory(tempDir);
			assertCanAddAndReadTextFile(tempDir);

			recordedPath = tempDir;
		}

	}

	// ---

	@DisplayName("when a test class has a test method with a @Dir-annotated parameter")
	@Nested
	class WhenTestClassHasTestMethodWithDirParameterTests {

		@DisplayName("then the parameter is populated with a new readable and writeable temporary directory "
				+ "that lasts as long as the test")
		@Test
		void thenParameterIsPopulatedWithNewReadableAndWriteableTempDirThatLastsAsLongAsTheTest() {
			ExecutionResults executionResults = PioneerTestKit
					.executeTestClass(SingleTestMethodWithDirParameterTestCase.class);
			assertThat(executionResults).hasSingleSucceededTest();
			assertThat(SingleTestMethodWithDirParameterTestCase.recordedPath).doesNotExist();
		}

	}

	static class SingleTestMethodWithDirParameterTestCase {

		static Path recordedPath;

		@Test
		void theTest(@Dir Path tempDir) {
			assertEmptyReadableWriteableTemporaryDirectory(tempDir);
			assertCanAddAndReadTextFile(tempDir);

			recordedPath = tempDir;
		}

	}

	// ---

	@DisplayName("when a test class has a test method with a parameter annotated with "
			+ "@New(value = TemporaryDirectory.class, arguments = {\"tempDirPrefix\"}")
	@Nested
	class WhenTestClassHasTestMethodWithParameterAnnotatedWithNewTempDirWithArg {

		@DisplayName("then the parameter is populated with a new temporary directory "
				+ "that has the prefix \"tempDirPrefix\"")
		@Test
		void thenParameterIsPopulatedWithNewTempDirWithSuffixEquallingArg() {
			ExecutionResults executionResults = PioneerTestKit
					.executeTestClass(SingleTestMethodWithParameterWithNewTempDirAndArgTestCase.class);
			assertThat(executionResults).hasSingleSucceededTest();
		}

	}

	static class SingleTestMethodWithParameterWithNewTempDirAndArgTestCase {

		@Test
		void theTest(@New(value = TemporaryDirectory.class, arguments = { "tempDirPrefix" }) Path tempDir) {
			assertThat(ROOT_TMP_DIR.relativize(tempDir)).asString().startsWith("tempDirPrefix");
		}

	}

	// ---

	@DisplayName("when a test class has multiple test methods with a @New(TemporaryDirectory.class)-annotated parameter")
	@Nested
	class WhenTestClassHasMultipleTestMethodsWithNewTempDirAnnotatedParameterTests {

		@DisplayName("then the parameters are populated with new readable and writeable "
				+ "temporary directories that are torn down afterwards")
		@Test
		void thenParametersArePopulatedWithNewReadableAndWriteableTempDirsThatAreTornDownAfterwards() {
			ExecutionResults executionResults = PioneerTestKit
					.executeTestClass(TwoTestMethodsWithNewTempDirParameterTestCase.class);
			assertThat(executionResults).hasNumberOfSucceededTests(2);
			assertThat(TwoTestMethodsWithNewTempDirParameterTestCase.recordedPaths)
					.hasSize(2)
					.doesNotHaveDuplicates()
					.allSatisfy(path -> assertThat(path).doesNotExist());
		}

	}

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
	class WhenTestClassHasTestMethodWithMultipleNewTempDirAnnotatedParametersTests {

		@DisplayName("then the parameters are populated with new readable and writeable "
				+ "temporary directories that are torn down afterwards")
		@Test
		void thenParametersArePopulatedWithNewReadableAndWriteableTempDirsThatAreTornDownAfterwards() {
			ExecutionResults executionResults = PioneerTestKit
					.executeTestClass(SingleTestMethodWithTwoNewTempDirParametersTestCase.class);
			assertThat(executionResults).hasSingleSucceededTest();
			assertThat(SingleTestMethodWithTwoNewTempDirParametersTestCase.recordedPaths)
					.hasSize(2)
					.doesNotHaveDuplicates()
					.allSatisfy(path -> assertThat(path).doesNotExist());
		}

	}

	static class SingleTestMethodWithTwoNewTempDirParametersTestCase {

		static List<Path> recordedPaths = new CopyOnWriteArrayList<>();

		@Test
		void firstTest(@New(TemporaryDirectory.class) Path firstTempDir,
				@New(TemporaryDirectory.class) Path secondTempDir) {
			assertEmptyReadableWriteableTemporaryDirectory(firstTempDir);
			assertCanAddAndReadTextFile(firstTempDir);
			assertEmptyReadableWriteableTemporaryDirectory(secondTempDir);
			assertCanAddAndReadTextFile(secondTempDir);

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

	static class TestConstructorWithNewTempDirParameterTestCase {

		static List<Path> recordedPathsFromConstructor = new CopyOnWriteArrayList<>();

		private final Path recordedPath;

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

	@DisplayName("when trying to instantiate a TemporaryDirectory with the wrong number of arguments")
	@Nested
	class WhenTryingToInstantiateTempDirWithWrongNumberOfArguments {

		@DisplayName("then an exception mentioning the number of arguments is thrown")
		@Test
		void thenExceptionMentioningNumberOfArgumentsIsThrown() {
			ExecutionResults executionResults = PioneerTestKit
					.executeTestClass(NewTempDirWithWrongNumberOfArgumentsTestCase.class);
			executionResults
					.allEvents()
					.debug()
					.assertThatEvents()
					.haveExactly(//
						1, //
						finished(//
							throwable(//
								instanceOf(ParameterResolutionException.class), //
								message("Unable to create a resource from `" + TemporaryDirectory.class + "`"),
								cause(instanceOf(IllegalArgumentException.class),
									message("Expected 0 or 1 arguments, but got 2")))));

		}

	}

	static class NewTempDirWithWrongNumberOfArgumentsTestCase {

		@Test
		void theTest(@New(value = TemporaryDirectory.class, arguments = { "1", "2" }) Path tempDir) {
			fail("We should not get this far.");
		}

	}

	// ---

	@DisplayName("when a new resource factory is applied to a parameter")
	@Nested
	class WhenNewResourceFactoryAppliedToParameterTests {

		@DisplayName("and the factory throws on ::create")
		@Nested
		class AndFactoryThrowsOnCreateTests {

			@DisplayName("then the thrown exception is wrapped and propagated")
			@Test
			void thenThrownExceptionIsWrappedAndPropagated() {
				ExecutionResults executionResults = PioneerTestKit.executeTestClass(ThrowOnNewRFCreateTestCase.class);
				executionResults
						.allEvents()
						.debug()
						.assertThatEvents()
						.haveExactly(//
							1, //
							finished(//
								throwable(//
									instanceOf(ParameterResolutionException.class), //
									message("Unable to create a resource from `" + ThrowOnRFCreateResourceFactory.class
											+ "`"), //
									cause(//
										instanceOf(EXPECTED_THROW_ON_RF_CREATE_EXCEPTION.getClass()), //
										message(EXPECTED_THROW_ON_RF_CREATE_EXCEPTION.getMessage())))));
			}

		}

		@DisplayName("and the factory throws on ::close")
		@Nested
		class AndFactoryThrowsOnCloseTests {

			@DisplayName("then the thrown exception is propagated")
			@Test
			void thenThrownExceptionIsPropagated() {
				ExecutionResults executionResults = PioneerTestKit.executeTestClass(ThrowOnNewRFCloseTestCase.class);
				executionResults
						.allEvents()
						.debug()
						.assertThatEvents()
						.haveExactly(//
							1, //
							finished(//
								throwable(//
									instanceOf(EXPECTED_THROW_ON_RF_CLOSE_EXCEPTION.getClass()), //
									message(EXPECTED_THROW_ON_RF_CLOSE_EXCEPTION.getMessage()))));
			}

		}

		@DisplayName("and a resource is created")
		@Nested
		class AndResourceIsCreatedTests {

			@DisplayName("and the resource throws on ::get")
			@Nested
			class AndResourceThrowsOnGetTests {

				@DisplayName("then the thrown exception is wrapped and propagated")
				@Test
				void thenThrownExceptionIsWrappedAndPropagated() {
					ExecutionResults executionResults = PioneerTestKit.executeTestClass(ThrowOnNewRGetTestCase.class);
					executionResults
							.allEvents()
							.debug()
							.assertThatEvents()
							.haveExactly(//
								1, //
								finished(//
									throwable(//
										instanceOf(ParameterResolutionException.class), //
										message("Unable to get the contents of the resource created by `"
												+ ThrowOnRGetResourceFactory.class + "`"), //
										cause(//
											instanceOf(EXPECTED_THROW_ON_R_GET_EXCEPTION.getClass()), //
											message(EXPECTED_THROW_ON_R_GET_EXCEPTION.getMessage())))));
				}

			}

			@DisplayName("and the resource throws on ::close")
			@Nested
			class AndResourceThrowsOnCloseTests {

				@DisplayName("then the thrown exception is propagated")
				@Test
				void thenThrownExceptionIsWrappedAndPropagated() {
					ExecutionResults executionResults = PioneerTestKit.executeTestClass(ThrowOnNewRCloseTestCase.class);
					executionResults
							.allEvents()
							.debug()
							.assertThatEvents()
							.haveExactly(//
								1, //
								finished(//
									throwable(//
										instanceOf(EXPECTED_THROW_ON_R_CLOSE_EXCEPTION.getClass()), //
										message(EXPECTED_THROW_ON_R_CLOSE_EXCEPTION.getMessage()))));
				}

			}

		}

	}

	static class ThrowOnNewRFCreateTestCase {

		@Test
		@SuppressWarnings("unused")
		void foo(@New(ThrowOnRFCreateResourceFactory.class) Object object) {

		}

	}

	static final class ThrowOnRFCreateResourceFactory implements ResourceFactory<Object> {

		@Override
		public Resource<Object> create(List<String> arguments) throws Exception {
			throw EXPECTED_THROW_ON_RF_CREATE_EXCEPTION;
		}

	}

	private static final Exception EXPECTED_THROW_ON_RF_CREATE_EXCEPTION = new IOException(
		"failed to connect to the Matrix");

	static class ThrowOnNewRFCloseTestCase {

		@Test
		@SuppressWarnings("unused")
		void foo(@New(ThrowOnRFCloseResourceFactory.class) Object object) {

		}

	}

	static final class ThrowOnRFCloseResourceFactory implements ResourceFactory<Object> {

		@Override
		public Resource<Object> create(List<String> arguments) {
			return () -> "foo";
		}

		@Override
		public void close() throws Exception {
			throw EXPECTED_THROW_ON_RF_CLOSE_EXCEPTION;
		}

	}

	private static final Exception EXPECTED_THROW_ON_RF_CLOSE_EXCEPTION = new CloneNotSupportedException(
		"failed to clone a homunculus");

	static class ThrowOnNewRGetTestCase {

		@Test
		@SuppressWarnings("unused")
		void foo(@New(ThrowOnRGetResourceFactory.class) Object object) {

		}

	}

	static final class ThrowOnRGetResourceFactory implements ResourceFactory<Object> {

		@Override
		public Resource<Object> create(List<String> arguments) {
			return new ThrowOnRGetResource();
		}

	}

	static final class ThrowOnRGetResource implements Resource<Object> {

		@Override
		public Object get() throws Exception {
			throw EXPECTED_THROW_ON_R_GET_EXCEPTION;
		}

	}

	private static final Exception EXPECTED_THROW_ON_R_GET_EXCEPTION = new FileAlreadyExistsException(
		"wait, what's that file doing there?");

	static class ThrowOnNewRCloseTestCase {

		@Test
		@SuppressWarnings("unused")
		void foo(@New(ThrowOnRCloseResourceFactory.class) Object object) {

		}

	}

	static final class ThrowOnRCloseResourceFactory implements ResourceFactory<Object> {

		@Override
		public Resource<Object> create(List<String> arguments) {
			return new ThrowOnRCloseResource();
		}

	}

	static final class ThrowOnRCloseResource implements Resource<Object> {

		@Override
		public Object get() throws Exception {
			return "foo";
		}

		@Override
		public void close() throws Exception {
			throw EXPECTED_THROW_ON_R_CLOSE_EXCEPTION;
		}

	}

	private static final Exception EXPECTED_THROW_ON_R_CLOSE_EXCEPTION = new UnknownHostException(
		"wait, where's the Internet gone?!");

	// ---

	@DisplayName("when a test class has a test method with a parameter annotated with both @New and @Shared")
	@Nested
	class WhenTestClassHasTestMethodWithParameterAnnotatedWithBothNewAndShared {

		@DisplayName("then an exception is thrown")
		@Test
		void thenExceptionIsThrown() {
			ExecutionResults executionResults = PioneerTestKit
					.executeTestClass(TestMethodWithParameterAnnotatedWithBothNewAndShared.class);
			assertThat(executionResults)
					.hasSingleFailedTest()
					.withExceptionInstanceOf(ParameterResolutionException.class)
					.hasMessageStartingWith("Parameter [java.nio.file.Path ")
					.hasMessageEndingWith("] in method "
							+ "[void org.junitpioneer.jupiter.ResourcesTests$TestMethodWithParameterAnnotatedWithBothNewAndShared.theTest(java.nio.file.Path)] "
							+ "is annotated with both @New and @Shared");
		}

	}

	static class TestMethodWithParameterAnnotatedWithBothNewAndShared {

		@Test
		void theTest(
				@New(TemporaryDirectory.class) @Shared(factory = TemporaryDirectory.class, name = "some-name") Path tempDir) {
			fail("We should not get this far.");
		}

	}

	// ---

	@DisplayName("when a test class has a test method with a "
			+ "@Shared(factory = TemporaryDirectory.class, name = \"some-name\")-annotated parameter")
	@Nested
	class WhenTestClassHasTestMethodWithSharedTempDirParameterTests {

		@DisplayName("then the parameter is populated with a new readable and writeable temporary directory "
				+ "that lasts as long as the test suite")
		@Test
		void thenParameterIsPopulatedWithNewReadableAndWriteableTempDirThatLastsAsLongAsTheTestSuite() {
			ExecutionResults executionResults = PioneerTestKit
					.executeTestClass(SingleTestMethodWithSharedTempDirParameterTestCase.class);
			assertThat(executionResults).hasSingleSucceededTest();
			assertThat(SingleTestMethodWithSharedTempDirParameterTestCase.recordedPath).doesNotExist();
		}

	}

	static class SingleTestMethodWithSharedTempDirParameterTestCase {

		static Path recordedPath;

		@Test
		void theTest(@Shared(factory = TemporaryDirectory.class, name = "some-name") Path tempDir) {
			assertEmptyReadableWriteableTemporaryDirectory(tempDir);
			assertCanAddAndReadTextFile(tempDir);

			recordedPath = tempDir;
		}

	}

	// ---

	@DisplayName("when a test class has a test method with a parameter annotated with "
			+ "@Shared(factory = TemporaryDirectory.class, name = \"some-name\", arguments = {\"tempDirPrefix\"}")
	@Nested
	class WhenTestClassHasTestMethodWithParameterAnnotatedWithSharedTempDirWithArg {

		@DisplayName("then the parameter is populated with a shared temporary directory "
				+ "that has the prefix \"tempDirPrefix\"")
		@Test
		void thenParameterIsPopulatedWithSharedTempDirWithSuffixEquallingArg() {
			ExecutionResults executionResults = PioneerTestKit
					.executeTestClass(SingleTestMethodWithParameterWithSharedTempDirAndArgTestCase.class);
			assertThat(executionResults).hasSingleSucceededTest();
		}

	}

	static class SingleTestMethodWithParameterWithSharedTempDirAndArgTestCase {

		@Test
		void theTest( //
				@Shared( //
						factory = TemporaryDirectory.class, //
						name = "some-name", //
						arguments = { "tempDirPrefix" }) //
				Path tempDir) {
			assertThat(ROOT_TMP_DIR.relativize(tempDir)).asString().startsWith("tempDirPrefix");
		}

	}

	// ---

	@DisplayName("when a test class has a test method with multiple @Shared(factory = TemporaryDirectory.class, name = \"...\")-annotated parameters with different names")
	@Nested
	class WhenTestClassHasTestMethodWithMultipleSharedTempDirAnnotatedParametersWithDifferentNamesTests {

		@DisplayName("then the parameters are populated with different readable and writeable "
				+ "temporary directories that are torn down afterwards")
		@Test
		void thenParametersArePopulatedWithDifferentReadableAndWriteableTempDirsThatAreTornDownAfterwards() {
			ExecutionResults executionResults = PioneerTestKit
					.executeTestClass(SingleTestMethodWithTwoDifferentSharedTempDirParametersTestCase.class);
			assertThat(executionResults).hasSingleSucceededTest();
			assertThat(SingleTestMethodWithTwoDifferentSharedTempDirParametersTestCase.recordedPaths)
					.hasSize(2)
					.doesNotHaveDuplicates()
					.allSatisfy(path -> assertThat(path).doesNotExist());
		}

	}

	static class SingleTestMethodWithTwoDifferentSharedTempDirParametersTestCase {

		static List<Path> recordedPaths = new CopyOnWriteArrayList<>();

		@Test
		void theTest(@Shared(factory = TemporaryDirectory.class, name = "first-name") Path firstTempDir,
				@Shared(factory = TemporaryDirectory.class, name = "second-name") Path secondTempDir) {
			assertEmptyReadableWriteableTemporaryDirectory(firstTempDir);
			assertCanAddAndReadTextFile(firstTempDir);
			assertEmptyReadableWriteableTemporaryDirectory(secondTempDir);
			assertCanAddAndReadTextFile(secondTempDir);

			recordedPaths.add(firstTempDir);
			recordedPaths.add(secondTempDir);
		}

	}

	// ---

	@DisplayName("when a test class has multiple test methods with a "
			+ "@Shared(factory = TemporaryDirectory.class, name = \"some-name\")-annotated parameter")
	@Nested
	class WhenTestClassHasMultipleTestMethodsWithParameterWithSameNamedSharedTempDirTests {

		@DisplayName("then the parameters are populated with a shared readable and writeable "
				+ "temporary directory that is torn down afterwards")
		@Test
		void thenParametersArePopulatedWithSharedReadableAndWriteableTempDirThatIsTornDownAfterwards() {
			ExecutionResults executionResults = PioneerTestKit
					.executeTestClass(TwoTestMethodsWithSharedSameNameTempDirParameterTestCase.class);
			assertThat(executionResults).hasNumberOfSucceededTests(2);
			assertThat(TwoTestMethodsWithSharedSameNameTempDirParameterTestCase.recordedPaths)
					.hasSize(2)
					.satisfies(allElementsAreEqual())
					.allSatisfy(path -> assertThat(path).doesNotExist());
		}

	}

	static class TwoTestMethodsWithSharedSameNameTempDirParameterTestCase {

		static List<Path> recordedPaths = new CopyOnWriteArrayList<>();

		@Test
		void firstTest(@Shared(factory = TemporaryDirectory.class, name = "some-name") Path tempDir) {
			assertReadableWriteableTemporaryDirectory(tempDir);
			assertCanAddAndReadTextFile(tempDir);

			recordedPaths.add(tempDir);
		}

		@Test
		void secondTest(@Shared(factory = TemporaryDirectory.class, name = "some-name") Path tempDir) {
			assertReadableWriteableTemporaryDirectory(tempDir);
			assertCanAddAndReadTextFile(tempDir);

			recordedPaths.add(tempDir);
		}

	}

	// ---

	@DisplayName("when two test classes have a test method with a "
			+ "@Shared(factory = TemporaryDirectory.class, name = \"some-name\")-annotated parameter")
	@Nested
	class WhenTwoTestClassesHaveATestMethodWithParameterWithSameNamedSharedTempDirTests {

		@DisplayName("then the parameters are populated with a shared readable and writeable "
				+ "temporary directory that is torn down afterwards")
		@Test
		void thenParametersArePopulatedWithSharedReadableAndWriteableTempDirThatIsTornDownAfterwards() {
			ExecutionResults executionResults = PioneerTestKit
					.executeTestClasses( //
						asList( //
							FirstSingleTestMethodWithSharedTempDirParameterTestCase.class,
							SecondSingleTestMethodWithSharedTempDirParameterTestCase.class));
			assertThat(executionResults).hasNumberOfSucceededTests(2);
			assertThat( //
				asList( //
					FirstSingleTestMethodWithSharedTempDirParameterTestCase.recordedPath,
					SecondSingleTestMethodWithSharedTempDirParameterTestCase.recordedPath))
							.satisfies(allElementsAreEqual())
							.allSatisfy(path -> assertThat(path).doesNotExist());
		}

	}

	static class FirstSingleTestMethodWithSharedTempDirParameterTestCase {

		static Path recordedPath;

		@Test
		void theTest(@Shared(factory = TemporaryDirectory.class, name = "some-name") Path tempDir) {
			assertReadableWriteableTemporaryDirectory(tempDir);
			assertCanAddAndReadTextFile(tempDir);

			recordedPath = tempDir;
		}

	}

	static class SecondSingleTestMethodWithSharedTempDirParameterTestCase {

		static Path recordedPath;

		@Test
		void theTest(@Shared(factory = TemporaryDirectory.class, name = "some-name") Path tempDir) {
			assertReadableWriteableTemporaryDirectory(tempDir);
			assertCanAddAndReadTextFile(tempDir);

			recordedPath = tempDir;
		}

	}

	// ---

	@DisplayName("when a parameter is annotated with "
			+ "@Shared(factory = TemporaryDirectory.class, name = \"some-name\"), and another parameter is annotated with "
			+ "@Shared with the same name but a different factory type")
	@Nested
	class WhenParameterIsAnnotatedWithSharedAndAnotherParamIsAnnotatedWithSharedWithSameNameButDifferentFactoryType {

		@DisplayName("then it throws an exception")
		@Test
		void thenItThrowsAnException() {
			ExecutionResults executionResults = PioneerTestKit
					.executeTestClass(SingleTestMethodWithConflictingSharedTempDirParametersTestCase.class);
			executionResults
					.allEvents()
					.debug()
					.assertThatEvents()
					.haveExactly(//
						1, //
						finished(//
							throwable(//
								instanceOf(ParameterResolutionException.class), //
								message(String
										.format(
											"Two or more parameters are annotated with @Shared annotations with the "
													+ "name \"%s\" but with different factory classes",
											"some-name")))));
		}

	}

	static class SingleTestMethodWithConflictingSharedTempDirParametersTestCase {

		@Test
		void theTest(@Shared(factory = TemporaryDirectory.class, name = "some-name") Path first,
				@Shared(factory = OtherResourceFactory.class, name = "some-name") Path second) {

		}

	}

	static final class OtherResourceFactory implements ResourceFactory<Path> {

		@Override
		public Resource<Path> create(List<String> arguments) {
			return () -> null;
		}

	}

	// ---

	@DisplayName("when a test method has two parameters annotated with "
			+ "@Shared(factory = TemporaryDirectory.class, name = \"some-name\")")
	@Nested
	class WhenTestMethodHasTwoParamsAnnotatedWithSharedAnnotationWithSameFactoryAndName {

		@DisplayName("then it throws an exception")
		@Test
		void thenItThrowsAnException() {
			ExecutionResults executionResults = PioneerTestKit
					.executeTestClass(TestMethodWithTwoParamsWithSameSharedAnnotationTestCase.class);
			executionResults
					.allEvents()
					.debug()
					.assertThatEvents()
					.haveExactly(//
						1, //
						finished(//
							throwable(//
								instanceOf(ParameterResolutionException.class), //
								message("A test method has 2 parameters annotated with @Shared with the same "
										+ "factory type and name; this is redundant, so it is not allowed"))));
		}

	}

	static class TestMethodWithTwoParamsWithSameSharedAnnotationTestCase {

		@Test
		void theTest(@Shared(factory = TemporaryDirectory.class, name = "some-name") Path first,
				@Shared(factory = TemporaryDirectory.class, name = "some-name") Path second) {

		}

	}

	// ---

	@DisplayName("when a shared resource factory is applied to a parameter")
	@Nested
	class WhenSharedResourceFactoryAppliedToParameterTests {

		@DisplayName("and the factory throws on ::create")
		@Nested
		class AndFactoryThrowsOnCreateTests {

			@DisplayName("then the thrown exception is wrapped and propagated")
			@Test
			void thenThrownExceptionIsWrappedAndPropagated() {
				ExecutionResults executionResults = PioneerTestKit
						.executeTestClass(ThrowOnSharedRFCreateTestCase.class);
				executionResults
						.allEvents()
						.debug()
						.assertThatEvents()
						.haveExactly(//
							1, //
							finished(//
								throwable(//
									instanceOf(ParameterResolutionException.class), //
									message("Unable to create a resource from `" + ThrowOnRFCreateResourceFactory.class
											+ "`"), //
									cause(//
										instanceOf(EXPECTED_THROW_ON_RF_CREATE_EXCEPTION.getClass()), //
										message(EXPECTED_THROW_ON_RF_CREATE_EXCEPTION.getMessage())))));
			}

		}

		@DisplayName("and the factory throws on ::close")
		@Nested
		class AndFactoryThrowsOnCloseTests {

			@DisplayName("then the thrown exception is propagated")
			@Test
			void thenThrownExceptionIsPropagated() {
				ExecutionResults executionResults = PioneerTestKit.executeTestClass(ThrowOnSharedRFCloseTestCase.class);
				executionResults
						.allEvents()
						.debug()
						.assertThatEvents()
						.haveExactly(//
							1, //
							finished(//
								throwable(//
									instanceOf(EXPECTED_THROW_ON_RF_CLOSE_EXCEPTION.getClass()), //
									message(EXPECTED_THROW_ON_RF_CLOSE_EXCEPTION.getMessage()))));
			}

		}

		@DisplayName("and a resource is created")
		@Nested
		class AndResourceIsCreatedTests {

			@DisplayName("and the resource throws on ::get")
			@Nested
			class AndResourceThrowsOnGetTests {

				@DisplayName("then the thrown exception is wrapped and propagated")
				@Test
				void thenThrownExceptionIsWrappedAndPropagated() {
					ExecutionResults executionResults = PioneerTestKit
							.executeTestClass(ThrowOnSharedRGetTestCase.class);
					executionResults
							.allEvents()
							.debug()
							.assertThatEvents()
							.haveExactly(//
								1, //
								finished(//
									throwable(//
										instanceOf(ParameterResolutionException.class), //
										message("Unable to get the contents of the resource created by `"
												+ ThrowOnRGetResourceFactory.class + "`"), //
										cause(//
											instanceOf(EXPECTED_THROW_ON_R_GET_EXCEPTION.getClass()), //
											message(EXPECTED_THROW_ON_R_GET_EXCEPTION.getMessage())))));
				}

			}

			@DisplayName("and the resource throws on ::close")
			@Nested
			class AndResourceThrowsOnCloseTests {

				@DisplayName("then the thrown exception is propagated")
				@Test
				void thenThrownExceptionIsWrappedAndPropagated() {
					ExecutionResults executionResults = PioneerTestKit
							.executeTestClass(ThrowOnSharedRCloseTestCase.class);
					executionResults
							.allEvents()
							.debug()
							.assertThatEvents()
							.haveExactly(//
								1, //
								finished(//
									throwable(//
										instanceOf(EXPECTED_THROW_ON_R_CLOSE_EXCEPTION.getClass()), //
										message(EXPECTED_THROW_ON_R_CLOSE_EXCEPTION.getMessage()))));
				}

			}

		}

	}

	static class ThrowOnSharedRFCreateTestCase {

		@Test
		@SuppressWarnings("unused")
		void foo(@Shared(factory = ThrowOnRFCreateResourceFactory.class, name = "some-name") Object object) {

		}

	}

	static class ThrowOnSharedRFCloseTestCase {

		@Test
		@SuppressWarnings("unused")
		void foo(@Shared(factory = ThrowOnRFCloseResourceFactory.class, name = "some-name") Object object) {

		}

	}

	static class ThrowOnSharedRGetTestCase {

		@Test
		@SuppressWarnings("unused")
		void foo(@Shared(factory = ThrowOnRGetResourceFactory.class, name = "some-name") Object object) {

		}

	}

	static class ThrowOnSharedRCloseTestCase {

		@Test
		@SuppressWarnings("unused")
		void foo(@Shared(factory = ThrowOnRCloseResourceFactory.class, name = "some-name") Object object) {

		}

	}

	// ---

	@DisplayName("when a number of shared resources are used concurrently")
	@Nested
	class WhenANumberOfSharedResourcesAreUsedConcurrently {

		@Execution(SAME_THREAD)
		@RepeatedTest(50)
		void thenTestsDoNotRunInParallel() {
			// TODO: Consider replacing with a test written with
			//   JCStress [1] or kotlinx-lincheck [2], as this test
			//   is not guaranteed to fail even after 50 tries.
			//   Refer to [3] for an example written with JCStress.
			//
			// [1] https://github.com/openjdk/jcstress
			// [2] https://github.com/Kotlin/kotlinx-lincheck
			// [3] https://github.com/openjdk/jcstress/blob/master/jcstress-samples/src/main/java/org/openjdk/jcstress/samples/problems/classic/Classic_01_DiningPhilosophers.java
			ExecutionResults executionResults = assertTimeoutPreemptively(Duration.ofSeconds(15),
				() -> PioneerTestKit.executeTestClass(ThrowIfTestsRunConcurrentlyTestCase.class),
				"The tests in ThrowIfTestsRunConcurrentlyTestCase became deadlocked!");
			assertThat(executionResults).hasNumberOfSucceededTests(3);
		}

		@Execution(SAME_THREAD)
		@RepeatedTest(50)
		void thenTestFactoriesDoNotRunInParallel() {
			ExecutionResults executionResults = assertTimeoutPreemptively(Duration.ofSeconds(15),
				() -> PioneerTestKit.executeTestClass(ThrowIfTestFactoriesRunConcurrentlyTestCase.class),
				"The tests in ThrowIfTestFactoriesRunConcurrentlyTestCase became deadlocked!");
			assertThat(executionResults).hasNumberOfSucceededTests(9);
		}

		@Execution(SAME_THREAD)
		@RepeatedTest(50)
		void thenTestTemplatesDoNotRunInParallel() {
			ExecutionResults executionResults = assertTimeoutPreemptively(Duration.ofSeconds(15),
				() -> PioneerTestKit.executeTestClass(ThrowIfTestTemplatesRunConcurrentlyTestCase.class),
				"The tests in ThrowIfTestTemplatesRunConcurrentlyTestCase became deadlocked!");
			assertThat(executionResults).hasNumberOfSucceededTests(9);
		}

		@Execution(SAME_THREAD)
		@RepeatedTest(50)
		void thenTestClassConstructorsDoNotRunInParallel() {
			ExecutionResults executionResults = assertTimeoutPreemptively(Duration.ofSeconds(15),
				() -> PioneerTestKit
						.executeTestClasses(asList(ThrowIfTestClassConstructorsRunConcurrentlyTestCase1.class,
							ThrowIfTestClassConstructorsRunConcurrentlyTestCase2.class,
							ThrowIfTestClassConstructorsRunConcurrentlyTestCase3.class)),
				"The tests in ThrowIfTestTemplatesRunConcurrentlyTestCase became deadlocked!");
			assertThat(executionResults).hasNumberOfSucceededTests(3);
		}

	}

	private static final AtomicInteger COUNTER = new AtomicInteger(0);
	private static final int TIMEOUT_MILLIS = 100;
	private static final String SHARED_RESOURCE_A_NAME = "shared-resource-a";
	private static final String SHARED_RESOURCE_B_NAME = "shared-resource-b";
	private static final String SHARED_RESOURCE_C_NAME = "shared-resource-c";

	static class ThrowIfTestsRunConcurrentlyTestCase {

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
		// The purpose of the tests below is to check two things:
		// - The tests don't run in parallel.
		// - Even if they don't run in parallel, that they don't deadlock.
		//
		// [1] https://en.wikipedia.org/wiki/Dining_philosophers_problem

		@Test
		void test1(
				// we don't actually use the resources, we just have them injected to verify whether sharing the
				// same resources prevent the tests from running in parallel
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_A_NAME) Path directoryA,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_B_NAME) Path directoryB)
				throws Exception {
			failIfExecutedConcurrently("test1");
		}

		@Test
		void test2(
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_B_NAME) Path directoryB,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_C_NAME) Path directoryC)
				throws Exception {
			failIfExecutedConcurrently("test2");
		}

		@Test
		void test3(
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_C_NAME) Path directoryC,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_A_NAME) Path directoryA)
				throws Exception {
			failIfExecutedConcurrently("test3");
		}

	}

	static class ThrowIfTestFactoriesRunConcurrentlyTestCase {

		@TestFactory
		Stream<DynamicTest> test1(
				// we don't actually use the resources, we just have them injected to verify whether sharing the
				// same resources prevent the tests from running in parallel
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_A_NAME) Path directoryA,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_B_NAME) Path directoryB)
				throws Exception {
			failIfExecutedConcurrently("test1");
			return DynamicTest
					.stream(Stream.of("DynamicTest1", "DynamicTest2", "DynamicTest3"), name -> "test1" + name,
						ResourcesTests::failIfExecutedConcurrently);
		}

		@TestFactory
		Stream<DynamicTest> test2(
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_B_NAME) Path directoryB,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_C_NAME) Path directoryC)
				throws Exception {
			failIfExecutedConcurrently("test2");
			return DynamicTest
					.stream(Stream.of("DynamicTest1", "DynamicTest2", "DynamicTest3"), name -> "test2" + name,
						ResourcesTests::failIfExecutedConcurrently);
		}

		@TestFactory
		Stream<DynamicTest> test3(
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_C_NAME) Path directoryC,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_A_NAME) Path directoryA)
				throws Exception {
			failIfExecutedConcurrently("test3");
			return DynamicTest
					.stream(Stream.of("DynamicTest1", "DynamicTest2", "DynamicTest3"), name -> "test3" + name,
						ResourcesTests::failIfExecutedConcurrently);
		}

	}

	static class ThrowIfTestTemplatesRunConcurrentlyTestCase {

		@ParameterizedTest
		@ValueSource(ints = { 1, 2, 3 })
		void test1(@SuppressWarnings("unused") int iteration,
				// we don't actually use the resources, we just have them injected to verify whether sharing the
				// same resources prevent the tests from running in parallel
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_A_NAME) Path directoryA,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_B_NAME) Path directoryB)
				throws Exception {
			failIfExecutedConcurrently("test1Iteration" + iteration);
		}

		@ParameterizedTest
		@ValueSource(ints = { 1, 2, 3 })
		void test2(@SuppressWarnings("unused") int iteration,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_B_NAME) Path directoryB,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_C_NAME) Path directoryC)
				throws Exception {
			failIfExecutedConcurrently("test2Iteration" + iteration);
		}

		@ParameterizedTest
		@ValueSource(ints = { 1, 2, 3 })
		void test3(@SuppressWarnings("unused") int iteration,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_C_NAME) Path directoryC,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_A_NAME) Path directoryA)
				throws Exception {
			failIfExecutedConcurrently("test3Iteration" + iteration);
		}

	}

	static class ThrowIfTestClassConstructorsRunConcurrentlyTestCase1 {

		ThrowIfTestClassConstructorsRunConcurrentlyTestCase1(
				// we don't actually use the resources, we just have them injected to verify whether sharing the
				// same resources prevent the test constructors from running in parallel
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_A_NAME) Path directoryA,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_B_NAME) Path directoryB)
				throws Exception {
			failIfExecutedConcurrently("testConstructor1");
		}

		@Test
		void fakeTest() {}

	}

	static class ThrowIfTestClassConstructorsRunConcurrentlyTestCase2 {

		ThrowIfTestClassConstructorsRunConcurrentlyTestCase2(
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_B_NAME) Path directoryB,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_C_NAME) Path directoryC)
				throws Exception {
			failIfExecutedConcurrently("testConstructor2");
		}

		@Test
		void fakeTest() {}

	}

	static class ThrowIfTestClassConstructorsRunConcurrentlyTestCase3 {

		ThrowIfTestClassConstructorsRunConcurrentlyTestCase3(
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_C_NAME) Path directoryC,
				@SuppressWarnings("unused") @Shared(factory = TemporaryDirectory.class, name = SHARED_RESOURCE_A_NAME) Path directoryA)
				throws Exception {
			failIfExecutedConcurrently("testConstructor3");
		}

		@Test
		void fakeTest() {}

	}

	// this method is written to fail if it is executed at overlapping times in different threads
	private static void failIfExecutedConcurrently(String testName) throws InterruptedException {
		boolean wasZero = COUNTER.compareAndSet(0, 1);
		System.out.println(testName + ": wasZero = " + wasZero);
		assertThat(wasZero).isTrue();
		// wait for the next test to catch up and potentially fail
		Thread.sleep(TIMEOUT_MILLIS);
		boolean wasOne = COUNTER.compareAndSet(1, 2);
		System.out.println(testName + ": wasOne = " + wasOne);
		assertThat(wasOne).isTrue();
		// wait for the last test to catch up and potentially fail
		Thread.sleep(TIMEOUT_MILLIS);
		boolean wasTwo = COUNTER.compareAndSet(2, 0);
		System.out.println(testName + ": wasTwo = " + wasTwo);
		assertThat(wasTwo).isTrue();
	}

	// ---

	@DisplayName("check that all resource-related classes are final")
	@Test
	void checkThatAllResourceRelatedClassesAreFinal() {
		assertThat(TemporaryDirectory.class).isFinal();
		assertThat(ResourceExtension.class).isFinal();
	}

	// ---

	private static void assertEmptyReadableWriteableTemporaryDirectory(Path tempDir) {
		assertThat(tempDir).isEmptyDirectory().startsWith(ROOT_TMP_DIR).isReadable().isWritable();
	}

	private static void assertReadableWriteableTemporaryDirectory(Path tempDir) {
		assertThat(tempDir).startsWith(ROOT_TMP_DIR).isReadable().isWritable();
	}

	private static void assertCanAddAndReadTextFile(Path tempDir) {
		try {
			Path testFile = Files.createTempFile(tempDir, "some-test-file", ".txt");
			Files.write(testFile, singletonList("some-text"));
			assertThat(Files.readAllLines(testFile)).containsExactly("some-text");
		}
		catch (IOException e) {
			fail(e);
		}
	}

	private static final Path ROOT_TMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"));

}
