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

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class TemporaryDirectory implements ResourceFactory<Path> {

	@Override
	public Resource<Path> create(List<String> arguments) throws Exception {
		if (arguments.size() >= 2) {
			throw new IllegalArgumentException("'arguments' was expected to have 0 or 1 elements, but it did not");
		}
		String prefix = (arguments.size() == 1) ? arguments.get(0) : "";
		return new InnerResource(Files.createTempDirectory(requireNonNull(prefix, "Argument 0 is null")));
	}

	private static final class InnerResource implements Resource<Path> {

		private final Path tempDir;

		InnerResource(Path tempDir) {
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
