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

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

// TODO: JavaDoc
public final class TemporaryDirectory implements ResourceFactory<Path> {

	@Override
	public Resource<Path> create(List<String> arguments) throws Exception {
		if (arguments.size() >= 2) {
			throw new IllegalArgumentException("Expected 0 or 1 arguments, but got " + arguments.size());
		}
		String directoryPrefix = (arguments.size() == 1) ? arguments.get(0) : "";
		requireNonNull(directoryPrefix, "Argument 0 can't be null");
		return new TemporaryDirectoryResource(Files.createTempDirectory(directoryPrefix));
	}

	private static final class TemporaryDirectoryResource implements Resource<Path> {

		private final Path tempDir;

		TemporaryDirectoryResource(Path tempDir) {
			this.tempDir = tempDir;
		}

		@Override
		public Path get() throws Exception {
			return tempDir;
		}

		@Override
		public void close() throws Exception {
			deleteRecursively(tempDir);
		}

	}

	private static void deleteRecursively(Path tempDir) throws IOException {
		Files.walkFileTree(tempDir, PathDeleter.INSTANCE);
	}

}
