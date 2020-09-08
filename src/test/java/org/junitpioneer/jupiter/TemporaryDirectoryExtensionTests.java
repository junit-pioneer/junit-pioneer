/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junitpioneer.testkit.PioneerTestKit.executeTestClass;
import static org.junitpioneer.testkit.PioneerTestKit.executeTestMethodWithParameterTypes;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junitpioneer.testkit.ExecutionResults;

@ExtendWith(TempDirectoryExtension.class)
class TemporaryDirectoryExtensionTests {

	private final static ConcurrentMap<String, Path> TEMPORARY_DIRECTORIES = new ConcurrentHashMap<>();

	private static void register(String methodName, Path path) {
		TEMPORARY_DIRECTORIES.merge(methodName, path, (__, existingPath) -> {
			throw new IllegalStateException("Test method " + methodName + " already registered a path");
		});
	}

	@AfterAll
	// needed to let IDEs run the test suite repeatedly
	static void clearTemporaryDirectories() {
		TEMPORARY_DIRECTORIES.clear();
	}

	@Test
	void tempDirExists(@TempDir Path dir) {
		assertThat(dir).exists();
	}

	@Test
	void tempDirCanStoreFiles(@TempDir Path dir) throws IOException, InterruptedException {
		List<String> writtenLines = Arrays
				.asList("worker bees can leave", "even drones can fly away", "the queen is their slave");

		Path file = Files.createFile(dir.resolve("tmp-file.txt"));
		Files.write(file, writtenLines);

		List<String> readLines = Files.lines(file).collect(Collectors.toList());
		assertThat(readLines).isEqualTo(writtenLines);
	}

	@Test
	void tempDirIsDeleted() {
		ExecutionResults results = executeTestMethodWithParameterTypes(TempDirDeletionTestCases.class,
			"tempDirWasDeleted", Path.class);
		assertThat(results).hasNumberOfSucceededTests(1);
		Path dir = TEMPORARY_DIRECTORIES.get("tempDirWasDeleted");

		assertThat(dir).doesNotExist();
	}

	@ExtendWith(TempDirectoryExtension.class)
	static class TempDirDeletionTestCases {

		@Test
		void tempDirWasDeleted(@TempDir Path dir) {
			register("tempDirWasDeleted", dir);
		}

	}

	/*
	 * Paths are treated identically once they're created, so it's not necessary to
	 * test the combination of all ways to create them with all requirements
	 */

	@Test
	void defaultDirectoryConfigurationWorks() {
		ExecutionResults results = executeTestClass(DefaultDirectoryTestCases.class);
		assertThat(results).hasNumberOfSucceededTests(1);
	}

	static class DefaultDirectoryTestCases {

		@RegisterExtension
		final TempDirectoryExtension inDefaultDirectory = TempDirectoryExtension.createInDefaultDirectory();

		@Test
		void tempDirExists(@TempDir Path dir) {
			assertThat(dir).exists();
		}

	}

	@Test
	void customDirectoryConfigurationWorks() {
		ExecutionResults results = executeTestClass(CustomDirectoryTestCases.class);
		assertThat(results).hasNumberOfSucceededTests(2);
	}

	static class CustomDirectoryTestCases {

		private static final Path CUSTOM_DIRECTORY;

		static {
			try {
				CUSTOM_DIRECTORY = Files.createTempDirectory("junit-pioneer-temp-dir-tests");
			}
			catch (IOException ex) {
				throw new UncheckedIOException(ex);
			}
		}

		@RegisterExtension
		final TempDirectoryExtension inCustomDirectory = TempDirectoryExtension
				.createInCustomDirectory(() -> CUSTOM_DIRECTORY);

		@Test
		void tempDirExists(@TempDir Path dir) {
			assertThat(dir).exists();
		}

		@Test
		void tempDirIsInCustomDir(@TempDir Path dir) {
			assertThat(dir).hasParent(CUSTOM_DIRECTORY);
		}

	}

}
