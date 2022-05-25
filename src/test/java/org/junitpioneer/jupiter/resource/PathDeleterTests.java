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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import org.junit.jupiter.api.Test;

class PathDeleterTests {

	@Test
	void deletesFile() throws IOException {
		try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
			Path file = fileSystem.getPath("file.txt");
			Files.createFile(file);

			Files.walkFileTree(file, PathDeleter.INSTANCE);

			assertThat(file).doesNotExist();
		}
	}

	@Test
	void deletingNonExistentFileProducesNoIOException() throws IOException {
		try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
			assertDoesNotThrow(
				() -> PathDeleter.INSTANCE.visitFile(fileSystem.getPath("some", "arbitrary", "file.txt"), null));
		}
	}

	@Test
	void deletesEmptyDirectory() throws IOException {
		try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
			Path dir = fileSystem.getPath("dir");
			Files.createDirectories(dir);

			Files.walkFileTree(dir, PathDeleter.INSTANCE);

			assertThat(dir).doesNotExist();
		}
	}

	@Test
	void deletesNonEmptyDirectory() throws IOException {
		try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
			Path dir = fileSystem.getPath("dir");
			Path file = dir.resolve("file.txt");
			Files.createDirectories(dir);
			Files.createFile(file);

			Files.walkFileTree(dir, PathDeleter.INSTANCE);

			assertThat(file).doesNotExist();
			assertThat(dir).doesNotExist();
		}
	}

	@Test
	void deletingNonExistentDirectoryProducesNoIOException() throws IOException {
		try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
			assertDoesNotThrow(() -> PathDeleter.INSTANCE
					.postVisitDirectory(fileSystem.getPath("some", "arbitrary", "directory"), null));
		}
	}

}
