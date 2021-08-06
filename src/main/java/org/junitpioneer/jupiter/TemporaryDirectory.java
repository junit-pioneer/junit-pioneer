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

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import static java.util.Objects.requireNonNull;

public final class TemporaryDirectory implements ResourceFactory<Path> {

	@Override
	public Resource<Path> create(List<String> arguments) throws Exception {
		if (arguments.size() >= 2) {
			throw new IllegalArgumentException("`arguments` was expected to have 0 or 1 elements, but it did not");
		}
		String prefix = (arguments.size() == 1) ? arguments.get(0) : "";
		return new InnerResource(prefix);
	}

	private static final class InnerResource implements Resource<Path> {

		private final String prefix;

		InnerResource(String prefix) {
			this.prefix = requireNonNull(prefix);
		}

		private Path tempDir;

		@Override
		public Path get() throws Exception {
			return (tempDir = Files.createTempDirectory(prefix));
		}

		@Override
		public void close() throws Exception {
			// TODO: Restore file permissions if needed.
			//       See: https://github.com/junit-team/junit5/issues/2609

			deleteRecursively(tempDir);
		}
	}

	private static void deleteRecursively(Path tempDir) throws IOException {
		// TODO: See how JUnit 5 recursively deletes temp directories, and if there's anything
		//       it does that we don't, write unit tests to reproduce their behaviour.
		Files.walkFileTree(tempDir, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				// TODO: Can we unit test that we don't throw an exception if
				//       the dir being deleted doesn't exist anymore due to
				//       a race condition?
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				// TODO: Can we unit test that we don't throw an exception if
				//       the dir being deleted doesn't exist anymore due to
				//       a race condition?
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}

		});
	}

}
