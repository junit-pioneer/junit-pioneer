/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.platform.engine.TestExecutionResult.Status.FAILED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;

import com.google.common.jimfs.Jimfs;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.test.event.ExecutionEvent;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junitpioneer.AbstractPioneerTestEngineTests;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

@DisplayName("TempDirectory extension")
class TempDirectoryTests extends AbstractPioneerTestEngineTests {

	@BeforeEach
	@AfterEach
	void resetStaticVariables() {
		BaseSharedTempDirTestCase.tempDir = Optional.empty();
		BaseSeparateTempDirsTestCase.tempDirs.clear();
	}

	@Nested
	@DisplayName("resolves shared temp dir")
	class SharedTempDir {

		@Test
		@DisplayName("when @TempDir is used on constructor parameter")
		void resolvesSharedTempDirWhenAnnotationIsUsedOnConstructorParameter() {
			assertResolvesShareTempDir(AnnotationOnConstructorParameterTestCase.class);
		}

		@Test
		@DisplayName("when @TempDir is used on @BeforeAll method parameter")
		void resolvesSharedTempDirWhenAnnotationIsUsedOnBeforeAllMethodParameter() {
			assertResolvesShareTempDir(AnnotationOnBeforeAllMethodParameterTestCase.class);
		}

		@Test
		@DisplayName("when @TempDir is used on constructor parameter with @TestInstance(PER_CLASS)")
		void resolvesSharedTempDirWhenAnnotationIsUsedOnConstructorParameterWithTestInstancePerClass() {
			assertResolvesShareTempDir(AnnotationOnConstructorParameterWithTestInstancePerClassTestCase.class);
		}

		@Test
		@DisplayName("when @TempDir is used on @BeforeAll method parameter with @TestInstance(PER_CLASS)")
		void resolvesSharedTempDirWhenAnnotationIsUsedOnBeforeAllMethodParameterWithTestInstancePerClass() {
			assertResolvesShareTempDir(AnnotationOnBeforeAllMethodParameterWithTestInstancePerClassTestCase.class);
		}

		private void assertResolvesShareTempDir(Class<? extends BaseSharedTempDirTestCase> testClass) {
			ExecutionEventRecorder executionEventRecorder = executeTests(testClass);

			assertEquals(2, executionEventRecorder.getTestStartedCount());
			assertEquals(0, executionEventRecorder.getTestFailedCount());
			assertEquals(2, executionEventRecorder.getTestSuccessfulCount());
			assertThat(BaseSharedTempDirTestCase.tempDir).isPresent();
			assertThat(BaseSharedTempDirTestCase.tempDir.get()).doesNotExist();
		}
	}

	@Nested
	@DisplayName("resolves separate temp dirs")
	class SeparateTempDirs {

		@Test
		@DisplayName("for @AfterAll method parameter when @TempDir is not used on constructor or @BeforeAll method parameter")
		void resolvesSeparateTempDirWhenAnnotationIsUsedOnAfterAllMethodParameterOnly() {
			ExecutionEventRecorder executionEventRecorder = executeTests(
				AnnotationOnAfterAllMethodParameterTestCase.class);

			assertEquals(1, executionEventRecorder.getTestStartedCount());
			assertEquals(0, executionEventRecorder.getTestFailedCount());
			assertEquals(1, executionEventRecorder.getTestSuccessfulCount());
			assertThat(AnnotationOnAfterAllMethodParameterTestCase.firstTempDir).isPresent();
			assertThat(AnnotationOnAfterAllMethodParameterTestCase.firstTempDir.get()).doesNotExist();
			assertThat(AnnotationOnAfterAllMethodParameterTestCase.secondTempDir).isPresent();
			assertThat(AnnotationOnAfterAllMethodParameterTestCase.secondTempDir.get()).doesNotExist();
		}

		@Test
		@DisplayName("when @TempDir is used on @BeforeEach/@AfterEach method parameters")
		void resolvesSeparateTempDirsWhenUsedOnForEachLifecycleMethods() {
			assertResolvesSeparateTempDirs(SeparateTempDirsWhenUsedOnForEachLifecycleMethodsTestCase.class);
			assertThat(BaseSeparateTempDirsTestCase.tempDirs.getFirst()).doesNotExist();
			assertThat(BaseSeparateTempDirsTestCase.tempDirs.getLast()).doesNotExist();
		}

		@Test
		@DisplayName("when @TempDir is used on @BeforeEach/@AfterEach method parameters with @TestInstance(PER_CLASS)")
		void resolvesSeparateTempDirsWhenUsedOnForEachLifecycleMethodsWithTestInstancePerClass() {
			assertResolvesSeparateTempDirs(
				SeparateTempDirsWhenUsedOnForEachLifecycleMethodsWithTestInstancePerClassTestCase.class);
			assertThat(BaseSeparateTempDirsTestCase.tempDirs.getFirst()).doesNotExist();
			assertThat(BaseSeparateTempDirsTestCase.tempDirs.getLast()).doesNotExist();
		}
	}

	@Nested
	@DisplayName("resolves temp dir with custom parent dir")
	class WithCustomParentDir {
		@Test
		@DisplayName("from Callable<Path>")
		void resolvesTempDirWithCustomParentDirFromCallable() {
			assertResolvesSeparateTempDirs(ParentDirFromCallableTestCase.class);
		}

		@Test
		@DisplayName("from ParentDirProvider")
		void resolvesTempDirWithCustomParentDirFromProvider() {
			assertResolvesSeparateTempDirs(ParentDirFromProviderTestCase.class);
		}
	}

	@Nested
	@DisplayName("reports failure")
	class Failures {

		@Test
		@DisplayName("when @TempDir is used on parameter of wrong type")
		void onlySupportsParametersOfTypePath() {
			ExecutionEventRecorder executionEventRecorder = executeTests(InvalidTestCase.class);

			assertSingleFailedTest(executionEventRecorder, ParameterResolutionException.class,
				"Can only resolve parameter of type java.nio.file.Path");
		}

		@Test
		@DisplayName("when attempt to create temp dir fails")
		void failedCreationAttemptMakesTestFail() {
			ExecutionEventRecorder executionEventRecorder = executeTests(FailedCreationAttemptTestCase.class);

			assertSingleFailedTest(executionEventRecorder, ParameterResolutionException.class,
				"Failed to create custom temp directory");
		}

		@Test
		@DisplayName("when attempt to delete temp dir fails")
		void failedDeletionAttemptMakesTestFail() {
			ExecutionEventRecorder executionEventRecorder = executeTests(FailedDeletionAttemptTestCase.class);

			assertSingleFailedTest(executionEventRecorder, IOException.class, "Failed to delete temp directory");
		}

		@Test
		@DisplayName("when attempt to get parent dir from ParentDirProvider fails")
		void erroneousParentDirProviderMakesTestFail() {
			ExecutionEventRecorder executionEventRecorder = executeTests(ErroneousParentDirProviderTestCase.class);

			assertSingleFailedTest(executionEventRecorder, ParameterResolutionException.class,
				"Failed to get parent directory");
		}

		private void assertSingleFailedTest(ExecutionEventRecorder executionEventRecorder,
				Class<? extends Throwable> clazz, String message) {
			assertEquals(1, executionEventRecorder.getTestStartedCount());
			assertEquals(1, executionEventRecorder.getTestFailedCount());
			assertEquals(0, executionEventRecorder.getTestSuccessfulCount());

			ExecutionEvent executionEvent = executionEventRecorder.getFailedTestFinishedEvents().get(0);
			Optional<TestExecutionResult> result = executionEvent.getPayload(TestExecutionResult.class);
			assertThat(result).isPresent();
			assertThat(result.get().getStatus()).isEqualTo(FAILED);
			Optional<Throwable> throwable = result.get().getThrowable();
			assertThat(throwable).containsInstanceOf(clazz);
			assertThat(throwable.get()).hasMessageContaining(message);
		}
	}

	private void assertResolvesSeparateTempDirs(Class<? extends BaseSeparateTempDirsTestCase> testClass) {
		ExecutionEventRecorder executionEventRecorder = executeTests(testClass);

		assertEquals(2, executionEventRecorder.getTestStartedCount());
		assertEquals(0, executionEventRecorder.getTestFailedCount());
		assertEquals(2, executionEventRecorder.getTestSuccessfulCount());
		Deque<Path> tempDirs = BaseSeparateTempDirsTestCase.tempDirs;
		assertThat(tempDirs).hasSize(2);
	}

	@ExtendWith(TempDirectory.class)
	static class BaseSharedTempDirTestCase {
		static Optional<Path> tempDir;

		@BeforeEach
		void beforeEach(@TempDir Path tempDir) {
			check(tempDir);
		}

		@Test
		void test1(@TempDir Path tempDir, TestInfo testInfo) throws Exception {
			check(tempDir);
			writeFile(tempDir, testInfo);
		}

		@Test
		void test2(TestInfo testInfo, @TempDir Path tempDir) throws Exception {
			check(tempDir);
			writeFile(tempDir, testInfo);
		}

		@AfterEach
		void afterEach(@TempDir Path tempDir) {
			check(tempDir);
		}

		static void check(Path tempDir) {
			assertThat(BaseSharedTempDirTestCase.tempDir).isPresent();
			assertSame(BaseSharedTempDirTestCase.tempDir.get(), tempDir);
			assertTrue(Files.exists(tempDir));
		}
	}

	static class AnnotationOnConstructorParameterTestCase extends BaseSharedTempDirTestCase {
		AnnotationOnConstructorParameterTestCase(@TempDir Path tempDir) {
			if (BaseSharedTempDirTestCase.tempDir.isPresent()) {
				assertSame(BaseSharedTempDirTestCase.tempDir.get(), tempDir);
			}
			else {
				BaseSharedTempDirTestCase.tempDir = Optional.of(tempDir);
			}
			check(tempDir);
		}
	}

	static class AnnotationOnBeforeAllMethodParameterTestCase extends BaseSharedTempDirTestCase {
		@BeforeAll
		static void beforeAll(@TempDir Path tempDir) {
			assertThat(BaseSharedTempDirTestCase.tempDir).isEmpty();
			BaseSharedTempDirTestCase.tempDir = Optional.of(tempDir);
			check(tempDir);
		}
	}

	@TestInstance(PER_CLASS)
	static class AnnotationOnConstructorParameterWithTestInstancePerClassTestCase
			extends AnnotationOnConstructorParameterTestCase {
		AnnotationOnConstructorParameterWithTestInstancePerClassTestCase(@TempDir Path tempDir) {
			super(tempDir);
		}
	}

	@TestInstance(PER_CLASS)
	static class AnnotationOnBeforeAllMethodParameterWithTestInstancePerClassTestCase
			extends AnnotationOnBeforeAllMethodParameterTestCase {
	}

	@ExtendWith(TempDirectory.class)
	static class AnnotationOnAfterAllMethodParameterTestCase {
		static Optional<Path> firstTempDir = Optional.empty();
		static Optional<Path> secondTempDir = Optional.empty();

		@Test
		void test(@TempDir Path tempDir, TestInfo testInfo) throws Exception {
			assertThat(firstTempDir).isEmpty();
			firstTempDir = Optional.of(tempDir);
			writeFile(tempDir, testInfo);
		}

		@AfterAll
		static void afterAll(@TempDir Path tempDir) {
			assertThat(firstTempDir).isPresent();
			assertNotEquals(firstTempDir.get(), tempDir);
			secondTempDir = Optional.of(tempDir);
		}
	}

	static class BaseSeparateTempDirsTestCase {
		static final Deque<Path> tempDirs = new LinkedList<>();

		@BeforeEach
		void beforeEach(@TempDir Path tempDir) {
			for (Path dir : tempDirs) {
				assertThat(dir).doesNotExist();
			}
			assertThat(tempDirs).doesNotContain(tempDir);
			tempDirs.add(tempDir);
			check(tempDir);
		}

		@Test
		void test1(@TempDir Path tempDir, TestInfo testInfo) throws Exception {
			check(tempDir);
			writeFile(tempDir, testInfo);
		}

		@Test
		void test2(TestInfo testInfo, @TempDir Path tempDir) throws Exception {
			check(tempDir);
			writeFile(tempDir, testInfo);
		}

		@AfterEach
		void afterEach(@TempDir Path tempDir) {
			check(tempDir);
		}

		void check(@TempDir Path tempDir) {
			assertSame(tempDirs.getLast(), tempDir);
			assertTrue(Files.exists(tempDir));
		}
	}

	@ExtendWith(TempDirectory.class)
	static class SeparateTempDirsWhenUsedOnForEachLifecycleMethodsTestCase extends BaseSeparateTempDirsTestCase {
	}

	@TestInstance(PER_CLASS)
	static class SeparateTempDirsWhenUsedOnForEachLifecycleMethodsWithTestInstancePerClassTestCase
			extends SeparateTempDirsWhenUsedOnForEachLifecycleMethodsTestCase {
	}

	@ExtendWith(TempDirectory.class)
	static class InvalidTestCase {
		@Test
		void wrongParameterType(@TempDir File ignored) {
			fail("this should never be called");
		}
	}

	static class ParentDirFromCallableTestCase extends BaseSeparateTempDirsTestCase {

		private static FileSystem fileSystem;

		@BeforeAll
		static void createFileSystem() {
			fileSystem = Jimfs.newFileSystem();
		}

		@AfterAll
		static void closeFileSystem() throws Exception {
			assertThat(tempDirs.getFirst()).doesNotExist();
			assertThat(tempDirs.getLast()).doesNotExist();
			fileSystem.close();
		}

		@RegisterExtension
		Extension tempDirectory = TempDirectory.createInCustomDirectory(
			() -> Files.createDirectories(fileSystem.getPath("tmp")));

	}

	static class ParentDirFromProviderTestCase extends BaseSeparateTempDirsTestCase {

		@RegisterExtension
		Extension tempDirectory = TempDirectory.createInCustomDirectory((parameterContext, extensionContext) -> {
			Store store = extensionContext.getRoot().getStore(Namespace.GLOBAL);
			FileSystem fileSystem = store.getOrComputeIfAbsent("jimfs.fileSystem", key -> new JimfsFileSystemResource(),
				JimfsFileSystemResource.class).get();
			return Files.createDirectories(fileSystem.getPath("tmp"));
		});

		static class JimfsFileSystemResource implements CloseableResource {
			private final FileSystem fileSystem;

			JimfsFileSystemResource() {
				this.fileSystem = Jimfs.newFileSystem();
			}

			FileSystem get() {
				return fileSystem;
			}

			@Override
			public void close() throws IOException {
				assertThat(tempDirs.getFirst()).doesNotExist();
				assertThat(tempDirs.getLast()).doesNotExist();
				fileSystem.close();
			}
		}
	}

	static class FailedCreationAttemptTestCase {

		private FileSystem fileSystem = mock(FileSystem.class);

		@BeforeEach
		void prepareFileSystem() {
			when(fileSystem.getPath(any())).thenAnswer(invocation -> {
				Path path = mock(Path.class, Arrays.toString(invocation.getArguments()));
				when(path.getFileSystem()).thenThrow(new RuntimeException("Simulated creation failure"));
				return path;
			});
		}

		@RegisterExtension
		Extension tempDirectory = TempDirectory.createInCustomDirectory(() -> fileSystem.getPath("tmp"));

		@Test
		void test(@TempDir Path tempDir) {
			fail("this should never be called");
		}
	}

	static class FailedDeletionAttemptTestCase {

		private FileSystem fileSystem = mock(FileSystem.class);

		@BeforeEach
		@SuppressWarnings("unchecked")
		void prepareFileSystem() throws Exception {
			FileSystemProvider provider = mock(FileSystemProvider.class);
			when(provider.readAttributes(any(), any(Class.class), any())) //
					.thenAnswer(invocation -> mock(invocation.getArgument(1)));
			doThrow(new IOException("Simulated deletion failure")).when(provider).delete(any());
			when(fileSystem.provider()).thenReturn(provider);
			when(fileSystem.getPath(any())).thenAnswer(invocation -> {
				Path path = mock(Path.class, Arrays.toString(invocation.getArguments()));
				when(path.getFileSystem()).thenReturn(fileSystem);
				when(path.toAbsolutePath()).thenReturn(path);
				when(path.resolve(any(Path.class))).thenAnswer(invocation1 -> invocation1.getArgument(0));
				when(path.toFile()).thenThrow(UnsupportedOperationException.class);
				when(path.relativize(any(Path.class))).thenAnswer(invocation1 -> invocation1.getArgument(0));
				return path;
			});
		}

		@RegisterExtension
		Extension tempDirectory = TempDirectory.createInCustomDirectory(() -> fileSystem.getPath("tmp"));

		@Test
		void test(@TempDir Path tempDir) {
			assertNotNull(tempDir);
		}
	}

	static class ErroneousParentDirProviderTestCase {

		private FileSystem fileSystem = mock(FileSystem.class);

		@RegisterExtension
		Extension tempDirectory = TempDirectory.createInCustomDirectory(() -> {
			throw new IOException("something went horribly wrong");
		});

		@Test
		void test(@TempDir Path tempDir) {
			fail("this should never be called");
		}
	}

	private static Path writeFile(@TempDir Path tempDir, TestInfo testInfo) throws IOException {
		Path file = tempDir.resolve(testInfo.getTestMethod().get().getName() + ".txt");
		Files.write(file, testInfo.getDisplayName().getBytes());
		return file;
	}
}
