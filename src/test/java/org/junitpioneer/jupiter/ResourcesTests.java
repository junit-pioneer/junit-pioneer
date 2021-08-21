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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.testkit.engine.EventConditions.finished;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.cause;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.throwable;
import static org.junitpioneer.internal.AllElementsAreEqual.allElementsAreEqual;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.UnknownHostException;
import java.nio.file.ClosedFileSystemException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junitpioneer.internal.TestExtensionContext;
import org.junitpioneer.internal.TestParameterContext;
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

	@Resources
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

	@Resources
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

	@DisplayName("when a test class has a test method with a @New(InMemoryDirectory.class)-annotated parameter")
	@Nested
	class WhenTestClassHasTestMethodWithNewInMemoryDirParameterTests {

		@DisplayName("then the parameter is populated with a new readable and writeable temporary directory "
				+ "that lasts as long as the test")
		@Test
		void thenParameterIsPopulatedWithNewReadableAndWriteableTempDirThatLastsAsLongAsTheTest() {
			ExecutionResults executionResults = PioneerTestKit
					.executeTestClass(SingleTestMethodWithNewInMemoryDirParameterTestCase.class);
			assertThat(executionResults).hasSingleSucceededTest();
			assertThatThrownBy(
				() -> Files.readAllLines(SingleTestMethodWithNewInMemoryDirParameterTestCase.recordedPath))
						.isInstanceOf(ClosedFileSystemException.class);
		}

	}

	@Resources
	static class SingleTestMethodWithNewInMemoryDirParameterTestCase {

		static Path recordedPath;

		@Test
		void theTest(@New(InMemoryDirectory.class) Path inMemoryDir) {
			assertEmptyReadableWriteableInMemoryDirectory(inMemoryDir);
			assertCanAddAndReadTextFile(inMemoryDir);

			recordedPath = inMemoryDir;
		}

	}

	static class InMemoryDirectory implements ResourceFactory<Path> {

		private final FileSystem inMemoryFileSystem = Jimfs.newFileSystem(Configuration.unix());

		@Override
		public Resource<Path> create(List<String> arguments) {
			return () -> {
				Path result = inMemoryFileSystem.getPath("test");
				Files.createDirectory(result);
				return result;
			};
		}

		@Override
		public void close() throws Exception {
			inMemoryFileSystem.close();
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

	@DisplayName("when new resource factory is applied to a parameter")
	@Nested
	class WhenNewResourceFactoryAppliedToParameterTests {

		@DisplayName("and the factory throws on ::create")
		@Nested
		class AndFactoryThrowsOnCreateTests {

			@DisplayName("then the thrown exception is wrapped and propagated")
			@Test
			void thenThrownExceptionIsWrappedAndPropagated() {
				ExecutionResults executionResults = PioneerTestKit
						.executeTestClass(ThrowOnCreateResourceFactoryTestCase.class);
				executionResults
						.testEvents()
						.debug()
						.assertThatEvents()
						.haveExactly(//
							1, //
							finished(//
								throwable(//
									instanceOf(ParameterResolutionException.class), //
									message(
										"Unable to create an instance of `" + ThrowOnCreateResourceFactory.class + "`"), //
									cause(//
										instanceOf(EXPECTED_THROW_ON_CREATE_RESOURCE_FACTORY_EXCEPTION.getClass()), //
										message(EXPECTED_THROW_ON_CREATE_RESOURCE_FACTORY_EXCEPTION.getMessage())))));
			}

		}

		@DisplayName("and the factory throws on ::close")
		@Nested
		class AndFactoryThrowsOnCloseTests {

			@DisplayName("then the thrown exception is propagated")
			@Test
			void thenThrownExceptionIsPropagated() {
				ExecutionResults executionResults = PioneerTestKit
						.executeTestClass(ThrowOnCloseResourceFactoryTestCase.class);
				executionResults
						.allEvents()
						.debug()
						.assertThatEvents()
						.haveExactly(//
							1, //
							finished(//
								throwable(//
									instanceOf(EXPECTED_THROW_ON_CLOSE_RESOURCE_FACTORY_EXCEPTION.getClass()), //
									message(EXPECTED_THROW_ON_CLOSE_RESOURCE_FACTORY_EXCEPTION.getMessage()))));
			}

		}

		@DisplayName("and a new resource is created")
		@Nested
		class AndNewResourceIsCreatedTests {

			@DisplayName("and the resource throws on ::get")
			@Nested
			class AndResourceThrowsOnGetTests {

				@DisplayName("then the thrown exception is wrapped and propagated")
				@Test
				void thenThrownExceptionIsWrappedAndPropagated() {
					ExecutionResults executionResults = PioneerTestKit
							.executeTestClass(ThrowOnGetResourceTestCase.class);
					executionResults
							.testEvents()
							.debug()
							.assertThatEvents()
							.haveExactly(//
								1, //
								finished(//
									throwable(//
										instanceOf(ParameterResolutionException.class), //
										message("Unable to create an instance of `" + ThrowOnGetResource.class + "`"), //
										cause(//
											instanceOf(EXPECTED_THROW_ON_GET_RESOURCE_EXCEPTION.getClass()), //
											message(EXPECTED_THROW_ON_GET_RESOURCE_EXCEPTION.getMessage())))));
				}

			}

			@DisplayName("and the resource throws on ::close")
			@Nested
			class AndResourceThrowsOnCloseTests {

				@DisplayName("then the thrown exception is propagated")
				@Test
				void thenThrownExceptionIsWrappedAndPropagated() {
					ExecutionResults executionResults = PioneerTestKit
							.executeTestClass(ThrowOnCloseResourceResourceFactoryTestCase.class);
					executionResults
							.testEvents()
							.debug()
							.assertThatEvents()
							.haveExactly(//
								1, //
								finished(//
									throwable(//
										instanceOf(EXPECTED_THROW_ON_CLOSE_RESOURCE_EXCEPTION.getClass()), //
										message(EXPECTED_THROW_ON_CLOSE_RESOURCE_EXCEPTION.getMessage()))));
				}

			}

		}

	}

	@Resources
	static class ThrowOnCreateResourceFactoryTestCase {

		@Test
		@SuppressWarnings("unused")
		void foo(@New(ThrowOnCreateResourceFactory.class) Object object) {

		}

	}

	static final class ThrowOnCreateResourceFactory implements ResourceFactory<Object> {

		@Override
		public Resource<Object> create(List<String> arguments) throws Exception {
			throw EXPECTED_THROW_ON_CREATE_RESOURCE_FACTORY_EXCEPTION;
		}

	}

	private static final Exception EXPECTED_THROW_ON_CREATE_RESOURCE_FACTORY_EXCEPTION = new IOException(
		"failed to connect to the Matrix");

	@Resources
	static class ThrowOnCloseResourceFactoryTestCase {

		@Test
		@SuppressWarnings("unused")
		void foo(@New(ThrowOnCloseResourceFactory.class) Object object) {

		}

	}

	static final class ThrowOnCloseResourceFactory implements ResourceFactory<Object> {

		@Override
		public Resource<Object> create(List<String> arguments) {
			return () -> "foo";
		}

		@Override
		public void close() throws Exception {
			throw EXPECTED_THROW_ON_CLOSE_RESOURCE_FACTORY_EXCEPTION;
		}

	}

	private static final Exception EXPECTED_THROW_ON_CLOSE_RESOURCE_FACTORY_EXCEPTION = new CloneNotSupportedException(
		"failed to clone a homunculus");

	@Resources
	static class ThrowOnGetResourceTestCase {

		@Test
		@SuppressWarnings("unused")
		void foo(@New(ThrowOnGetResourceResourceFactory.class) Object object) {

		}

	}

	static final class ThrowOnGetResourceResourceFactory implements ResourceFactory<Object> {

		@Override
		public Resource<Object> create(List<String> arguments) {
			return new ThrowOnGetResource();
		}

	}

	static final class ThrowOnGetResource implements Resource<Object> {

		@Override
		public Object get() throws Exception {
			throw EXPECTED_THROW_ON_GET_RESOURCE_EXCEPTION;
		}

	}

	private static final Exception EXPECTED_THROW_ON_GET_RESOURCE_EXCEPTION = new FileAlreadyExistsException(
		"wait, what's that file doing there?");

	@Resources
	static class ThrowOnCloseResourceResourceFactoryTestCase {

		@Test
		@SuppressWarnings("unused")
		void foo(@New(ThrowOnCloseResourceResourceFactory.class) Object object) {

		}

	}

	static final class ThrowOnCloseResourceResourceFactory implements ResourceFactory<Object> {

		@Override
		public Resource<Object> create(List<String> arguments) {
			return new Resource<Object>() {

				@Override
				public Object get() {
					return "foo";
				}

				@Override
				public void close() throws Exception {
					throw EXPECTED_THROW_ON_CLOSE_RESOURCE_EXCEPTION;
				}

			};
		}

	}

	private static final Exception EXPECTED_THROW_ON_CLOSE_RESOURCE_EXCEPTION = new UnknownHostException(
		"wait, where's the Internet gone?!");

	// ---

	// TODO: Consider writing tests that check what happens when trying to instantiate
	//       the class specified by an @New or @Shared annotation doesn't have a constructor
	//       with the matching number and types of arguments.

	// ---

	@DisplayName("when ResourceManagerExtension is unable to find @New on a parameter")
	@Nested
	class WhenResourceManagerExtensionUnableToFindNewOnParameterTests {

		@DisplayName("then an exception mentioning the parameter and the test method it's on is thrown")
		@Test
		void thenExceptionMentioningParameterAndTestMethodItsOnIsThrown() {
			Class<?> exampleClass = String.class;
			Method exampleMethod = ReflectionSupport.findMethod(exampleClass, "valueOf", Object.class).get();
			Parameter exampleParameter = exampleMethod.getParameters()[0];

			assertThatThrownBy(() -> new ResourceManagerExtension()
					.resolveParameter( //
						new TestParameterContext(exampleParameter),
						new TestExtensionContext(exampleClass, exampleMethod)))
								.isInstanceOf(ParameterResolutionException.class)
								.hasMessage("Parameter `" + exampleParameter + "` on method `" + exampleMethod
										+ "` is not annotated with @New");
		}

		@DisplayName("and the test method does not exist")
		@Nested
		class AndTestMethodDoesNotExistTests {

			@DisplayName("then an exception mentioning just the parameter is thrown")
			@Test
			void thenExceptionMentioningJustParameterIsThrown() {
				Class<?> exampleClass = String.class;
				Method exampleMethod = ReflectionSupport.findMethod(exampleClass, "valueOf", Object.class).get();
				Parameter exampleParameter = exampleMethod.getParameters()[0];

				assertThatThrownBy(() -> new ResourceManagerExtension()
						.resolveParameter(new TestParameterContext(exampleParameter),
							new TestExtensionContext(null, null)))
									.isInstanceOf(ParameterResolutionException.class)
									.hasMessage("Parameter `" + exampleParameter
											+ "` on unknown method is not annotated with @New");
			}

		}

	}

	// ---

	// TODO: Write and test with two custom ResourceFactory implementations: jimfs and OkHttp's MockWebServer

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

	@Resources
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

	@Resources
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

	@Resources
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

	@Resources
	static class TwoTestMethodsWithSharedSameNameTempDirParameterTestCase {

		static List<Path> recordedPaths = new CopyOnWriteArrayList<>();

		@Test
		void firstTest(@Shared(factory = TemporaryDirectory.class, name = "some-name") Path tempDir) {
			assertEmptyReadableWriteableTemporaryDirectory(tempDir);
			assertCanAddAndReadTextFile(tempDir);

			recordedPaths.add(tempDir);
		}

		@Test
		void secondTest(@Shared(factory = TemporaryDirectory.class, name = "some-name") Path tempDir) {
			assertEmptyReadableWriteableTemporaryDirectory(tempDir);
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

	@Resources
	static class FirstSingleTestMethodWithSharedTempDirParameterTestCase {

		static Path recordedPath;

		@Test
		void theTest(@Shared(factory = TemporaryDirectory.class, name = "some-name") Path tempDir) {
			assertEmptyReadableWriteableTemporaryDirectory(tempDir);
			assertCanAddAndReadTextFile(tempDir);

			recordedPath = tempDir;
		}

	}

	@Resources
	static class SecondSingleTestMethodWithSharedTempDirParameterTestCase {

		static Path recordedPath;

		@Test
		void theTest(@Shared(factory = TemporaryDirectory.class, name = "some-name") Path tempDir) {
			assertEmptyReadableWriteableTemporaryDirectory(tempDir);
			assertCanAddAndReadTextFile(tempDir);

			recordedPath = tempDir;
		}

	}

	// ---

	@DisplayName("check that all resource-related classes are final")
	@Test
	void checkThatAllResourceRelatedClassesAreFinal() {
		assertThat(TemporaryDirectory.class).isFinal();
		assertThat(ResourceManagerExtension.class).isFinal();
		// TODO: Add the jimfs and OkHttp MockServer-based resource factories here
	}

	// ---

	private static void assertEmptyReadableWriteableTemporaryDirectory(Path tempDir) {
		assertThat(tempDir).isEmptyDirectory().startsWith(ROOT_TMP_DIR).isReadable().isWritable();
	}

	private static void assertEmptyReadableWriteableInMemoryDirectory(Path tempDir) {
		assertThat(tempDir).isEmptyDirectory().isReadable().isWritable();
		try (FileSystem fileSystem = Jimfs.newFileSystem()) {
			assertThat(tempDir.getFileSystem()).isInstanceOf(fileSystem.getClass());
		}
		catch (IOException e) {
			fail(e);
		}
	}

	private static void assertCanAddAndReadTextFile(Path tempDir) {
		try {
			Path testFile = Files.createTempFile(tempDir, "some-test-file", ".txt");
			Files.write(testFile, singletonList("some-text"));
			assertThat(Files.readAllLines(testFile)).containsExactly("some-text");
			Files.delete(testFile);
		}
		catch (IOException e) {
			fail(e);
		}
	}

	private static final Path ROOT_TMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"));

}
