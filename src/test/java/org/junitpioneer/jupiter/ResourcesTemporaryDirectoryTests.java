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
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.testkit.engine.EventConditions.finished;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.cause;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.throwable;
import static org.junitpioneer.internal.AllElementsAreEqual.allElementsAreEqual;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

@DisplayName("Resources extension")
class ResourcesTemporaryDirectoryTests {

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

	@DisplayName("when a test class has a test method with a parameter annotated with "
			+ "@New(value = TemporaryDirectory.class, arguments = {\"tempDirPrefix\"}")
	@Nested
	class WhenTestClassHasTestMethodWithParameterAnnotatedWithNewTempDirWithArgTests {

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
	class WhenTryingToInstantiateTempDirWithWrongNumberOfArgumentsTests {

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

	@DisplayName("check that TemporaryDirectory is final")
	@Test
	void checkThatTemporaryDirectoryIsFinal() {
		assertThat(TemporaryDirectory.class).isFinal();
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
