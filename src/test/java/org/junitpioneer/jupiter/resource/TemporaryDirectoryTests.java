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
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.testkit.engine.EventConditions.finished;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.cause;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.throwable;
import static org.junitpioneer.internal.AllElementsAreEqual.allElementsAreEqual;
import static org.junitpioneer.jupiter.resource.MorePaths.rootTempDir;
import static org.junitpioneer.jupiter.resource.Scope.GLOBAL;
import static org.junitpioneer.jupiter.resource.Scope.SOURCE_FILE;
import static org.junitpioneer.jupiter.resource.TemporaryDirectoryAssertions.assertCanAddAndReadTextFile;
import static org.junitpioneer.jupiter.resource.TemporaryDirectoryAssertions.assertEmptyReadableWriteableTemporaryDirectory;
import static org.junitpioneer.jupiter.resource.TemporaryDirectoryAssertions.assertReadableWriteableTemporaryDirectory;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

@DisplayName("Resources extension")
class TemporaryDirectoryTests {

	@DisplayName("when a test class has a test method with a @New(TemporaryDirectory.class)-annotated parameter")
	@Nested
	class WhenTestClassHasTestMethodWithNewTempDirParameterTests {

		@DisplayName("then the parameter is populated with a new readable and writeable temporary directory "
				+ "that lasts as long as the test")
		@Test
		void thenParameterIsPopulatedWithNewReadableAndWriteableTempDirThatLastsAsLongAsTheTest() {
			ExecutionResults executionResults = PioneerTestKit
					.executeTestClass(SingleTestMethodWithNewTempDirParameterTestCases.class);
			assertThat(executionResults).hasSingleSucceededTest();
			assertThat(SingleTestMethodWithNewTempDirParameterTestCases.recordedPath).doesNotExist();
		}

	}

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
	static class SingleTestMethodWithNewTempDirParameterTestCases {

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
					.executeTestClass(SingleTestMethodWithParameterWithNewTempDirAndArgTestCases.class);
			assertThat(executionResults).hasSingleSucceededTest();
		}

	}

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
	static class SingleTestMethodWithParameterWithNewTempDirAndArgTestCases {

		@Test
		void theTest(@New(value = TemporaryDirectory.class, arguments = { "tempDirPrefix" }) Path tempDir) {
			assertThat(rootTempDir().relativize(tempDir)).asString().startsWith("tempDirPrefix");
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
					.executeTestClass(TwoTestMethodsWithNewTempDirParameterTestCases.class);
			assertThat(executionResults).hasNumberOfSucceededTests(2);
			assertThat(TwoTestMethodsWithNewTempDirParameterTestCases.recordedPaths)
					.hasSize(2)
					.doesNotHaveDuplicates()
					.allSatisfy(path -> assertThat(path).doesNotExist());
		}

	}

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
	static class TwoTestMethodsWithNewTempDirParameterTestCases {

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
					.executeTestClass(SingleTestMethodWithTwoNewTempDirParametersTestCases.class);
			assertThat(executionResults).hasSingleSucceededTest();
			assertThat(SingleTestMethodWithTwoNewTempDirParametersTestCases.recordedPaths)
					.hasSize(2)
					.doesNotHaveDuplicates()
					.allSatisfy(path -> assertThat(path).doesNotExist());
		}

	}

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
	static class SingleTestMethodWithTwoNewTempDirParametersTestCases {

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
					.executeTestClass(TestConstructorWithNewTempDirParameterTestCases.class);
			assertThat(executionResults).hasNumberOfSucceededTests(2);
			assertThat(TestConstructorWithNewTempDirParameterTestCases.recordedPathsFromConstructor)
					.hasSize(2)
					.doesNotHaveDuplicates()
					.allSatisfy(path -> assertThat(path).doesNotExist());
		}

	}

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
	static class TestConstructorWithNewTempDirParameterTestCases {

		static List<Path> recordedPathsFromConstructor = new CopyOnWriteArrayList<>();

		private final Path recordedPath;

		TestConstructorWithNewTempDirParameterTestCases(@New(TemporaryDirectory.class) Path tempDir) {
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
					.executeTestClass(NewTempDirWithWrongNumberOfArgumentsTestCases.class);
			executionResults
					.allEvents()
					.debug()
					.assertThatEvents()
					.haveExactly(//
						1, //
						finished(//
							throwable(//
								instanceOf(ParameterResolutionException.class), //
								message("Unable to create a resource from `" + TemporaryDirectory.class.getTypeName()
										+ "`"),
								cause(instanceOf(IllegalArgumentException.class),
									message("Expected 0 or 1 arguments, but got 2")))));
		}

	}

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
	static class NewTempDirWithWrongNumberOfArgumentsTestCases {

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
					.executeTestClass(SingleTestMethodWithSharedTempDirParameterTestCases.class);
			assertThat(executionResults).hasSingleSucceededTest();
			assertThat(SingleTestMethodWithSharedTempDirParameterTestCases.recordedPath).doesNotExist();
		}

	}

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
	static class SingleTestMethodWithSharedTempDirParameterTestCases {

		static Path recordedPath;

		@Test
		void theTest(@Shared(factory = TemporaryDirectory.class, name = "some-name") Path tempDir) {
			assertEmptyReadableWriteableTemporaryDirectory(tempDir);
			assertCanAddAndReadTextFile(tempDir);

			recordedPath = tempDir;
		}

	}

	// ---

	@DisplayName("when a test class has a test method with multiple "
			+ "@Shared(factory = TemporaryDirectory.class, name = \"...\")-annotated parameters with different names")
	@Nested
	class WhenTestClassHasTestMethodWithMultipleSharedTempDirAnnotatedParametersWithDifferentNamesTests {

		@DisplayName("then the parameters are populated with different readable and writeable "
				+ "temporary directories that are torn down afterwards")
		@Test
		void thenParametersArePopulatedWithDifferentReadableAndWriteableTempDirsThatAreTornDownAfterwards() {
			ExecutionResults executionResults = PioneerTestKit
					.executeTestClass(SingleTestMethodWithTwoDifferentSharedTempDirParametersTestCases.class);
			assertThat(executionResults).hasSingleSucceededTest();
			assertThat(SingleTestMethodWithTwoDifferentSharedTempDirParametersTestCases.recordedPaths)
					.hasSize(2)
					.doesNotHaveDuplicates()
					.allSatisfy(path -> assertThat(path).doesNotExist());
		}

	}

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
	static class SingleTestMethodWithTwoDifferentSharedTempDirParametersTestCases {

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
					.executeTestClass(TwoTestMethodsWithSharedSameNameTempDirParameterTestCases.class);
			assertThat(executionResults).hasNumberOfSucceededTests(2);
			assertThat(TwoTestMethodsWithSharedSameNameTempDirParameterTestCases.recordedPaths)
					.hasSize(2)
					.satisfies(allElementsAreEqual())
					.allSatisfy(path -> assertThat(path).doesNotExist());
		}

	}

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
	static class TwoTestMethodsWithSharedSameNameTempDirParameterTestCases {

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
			+ "@Shared(factory = TemporaryDirectory.class, name = \"some-name\", scope = GLOBAL)-annotated parameter")
	@Nested
	class WhenTwoTestClassesHaveATestMethodWithParameterWithSameNamedAndGloballyScopedSharedTempDirTests {

		@DisplayName("then the parameters are populated with a shared readable and writeable "
				+ "temporary directory that is torn down afterwards")
		@Test
		void thenParametersArePopulatedWithSharedReadableAndWriteableTempDirThatIsTornDownAfterwards() {
			ExecutionResults executionResults = PioneerTestKit
					.executeTestClasses( //
						asList( //
							FirstSingleTestMethodWithGlobalTempDirParameterTestCases.class,
							SecondSingleTestMethodWithGlobalTempDirParameterTestCases.class));
			assertThat(executionResults).hasNumberOfSucceededTests(2);
			assertThat(FirstSingleTestMethodWithGlobalTempDirParameterTestCases.recordedPath)
					.isEqualTo(SecondSingleTestMethodWithGlobalTempDirParameterTestCases.recordedPath);
			assertThat(FirstSingleTestMethodWithGlobalTempDirParameterTestCases.recordedPath).doesNotExist();
			assertThat(SecondSingleTestMethodWithGlobalTempDirParameterTestCases.recordedPath).doesNotExist();
		}

	}

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
	static class FirstSingleTestMethodWithGlobalTempDirParameterTestCases {

		static Path recordedPath;

		@Test
		void theTest(@Shared(factory = TemporaryDirectory.class, name = "some-name", scope = GLOBAL) Path tempDir) {
			assertReadableWriteableTemporaryDirectory(tempDir);
			assertCanAddAndReadTextFile(tempDir);

			recordedPath = tempDir;
		}

	}

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
	static class SecondSingleTestMethodWithGlobalTempDirParameterTestCases {

		static Path recordedPath;

		@Test
		void theTest(@Shared(factory = TemporaryDirectory.class, name = "some-name", scope = GLOBAL) Path tempDir) {
			assertReadableWriteableTemporaryDirectory(tempDir);
			assertCanAddAndReadTextFile(tempDir);

			recordedPath = tempDir;
		}

	}

	// ---

	@DisplayName("when two test classes in the same file have a test method with a "
			+ "@Shared(factory = TemporaryDirectory.class, name = \"some-name\", scope = SOURCE_FILE)-annotated "
			+ "parameter")
	@Nested
	class WhenTwoTestClassesInSameFileHaveTestMethodWithParameterWithSameNamedAndSourceFileScopedSharedTempDirTests {

		@DisplayName("then the parameters are populated with a shared readable and writeable "
				+ "temporary directory that is torn down afterwards")
		@Test
		void thenParametersArePopulatedWithSharedReadableAndWriteableTempDirsThatIsTornDownAfterwards() {
			ExecutionResults executionResults = PioneerTestKit
					.executeTestClass(SingleTestMethodWithSourceFileScopedTempDirParameterTestCases.class);
			assertThat(executionResults).hasNumberOfSucceededTests(2);
			assertThat(SingleTestMethodWithSourceFileScopedTempDirParameterTestCases.recordedPathForImplicit)
					.isEqualTo(SingleTestMethodWithSourceFileScopedTempDirParameterTestCases.recordedPathForExplicit);
			assertThat(SingleTestMethodWithSourceFileScopedTempDirParameterTestCases.recordedPathForImplicit)
					.doesNotExist();
			assertThat(SingleTestMethodWithSourceFileScopedTempDirParameterTestCases.recordedPathForExplicit)
					.doesNotExist();
		}

	}

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
	static class SingleTestMethodWithSourceFileScopedTempDirParameterTestCases {

		static Path recordedPathForImplicit;
		static Path recordedPathForExplicit;

		@Nested
		class Implicit {

			@Test
			void theTest(@Shared(factory = TemporaryDirectory.class, name = "some-name") Path tempDir) {
				recordedPathForImplicit = tempDir;
			}

		}

		@Nested
		class Explicit {

			@Test
			void theTest(
					@Shared(factory = TemporaryDirectory.class, name = "some-name", scope = SOURCE_FILE) Path tempDir) {
				recordedPathForExplicit = tempDir;
			}

		}

	}

	// ---

	@DisplayName("when two test classes in different files have a test method with a "
			+ "@Shared(factory = TemporaryDirectory.class, name = \"some-name\", scope = SOURCE_FILE)-annotated "
			+ "parameter")
	@Nested
	class WhenTwoTestClassesInDiffFilesHaveTestMethodWithParameterWithSameNamedAndSourceFileScopedSharedTempDirTests {

		@DisplayName("then the parameters are populated with unique readable and writeable "
				+ "temporary directories that that torn down afterwards")
		@Test
		void thenParametersArePopulatedWithUniqueReadableAndWriteableTempDirsThatAreTornDownAfterwards() {
			ExecutionResults executionResults = PioneerTestKit
					.executeTestClasses( //
						asList( //
							FirstSingleTopLevelTestMethodWithSourceFileScopedTempDirParameterTestCases.class,
							SecondSingleTopLevelTestMethodWithSourceFileScopedTempDirParameterTestCases.class));

			assertThat(executionResults).hasNumberOfSucceededTests(2);

			assertThat(FirstSingleTopLevelTestMethodWithSourceFileScopedTempDirParameterTestCases.recordedPath)
					.isNotEqualTo(
						SecondSingleTopLevelTestMethodWithSourceFileScopedTempDirParameterTestCases.recordedPath);

			assertThat(FirstSingleTopLevelTestMethodWithSourceFileScopedTempDirParameterTestCases.recordedPath)
					.doesNotExist();
			assertThat(SecondSingleTopLevelTestMethodWithSourceFileScopedTempDirParameterTestCases.recordedPath)
					.doesNotExist();
		}

	}

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
	static class FirstSingleTopLevelTestMethodWithSourceFileScopedTempDirParameterTestCases {

		static Path recordedPath;

		@Test
		void theTest(
				@Shared(factory = TemporaryDirectory.class, name = "some-name", scope = SOURCE_FILE) Path tempDir) {
			recordedPath = tempDir;
		}

	}

	// This test case class is static so that JUnit 5 doesn't pick it up
	// automatically but our own tests can still explicitly find it and run it.
	static class SecondSingleTopLevelTestMethodWithSourceFileScopedTempDirParameterTestCases {

		static Path recordedPath;

		@Test
		void theTest(
				@Shared(factory = TemporaryDirectory.class, name = "some-name", scope = SOURCE_FILE) Path tempDir) {
			recordedPath = tempDir;
		}

	}

	// ---

	@DisplayName("check that TemporaryDirectory is final")
	@Test
	void checkThatTemporaryDirectoryIsFinal() {
		assertThat(TemporaryDirectory.class).isFinal();
	}

}
