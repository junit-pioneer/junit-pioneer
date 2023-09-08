/*
 * Copyright 2016-2023 the original author or authors.
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

/**
 * {@code TemporaryDirectory} is a "resource factory" implementation that, combined with
 * {@link New @New} or {@link Shared @Shared}, allows for the creation of temporary directories
 * that can be used by individual tests or shared across multiple tests.
 *
 * <p>When used with the {@code @New} annotation and the annotation's {@code arguments} field is
 * populated, the first argument will be used as the <i>prefix</i> of the name of the temporary
 * directory.</p>
 *
 * <p>It is part of the "resources" JUnit Jupiter extension, which pertains to anything that needs
 * to be injected into tests and which may need to be started up or torn down. Temporary
 * directories are a common example.</p>
 *
 * <p>This class is intended for <i>users</i>.</p>
 *
 * <p>For more details and examples, see
 * <a href="https://junit-pioneer.org/docs/resources/" target="_top">the documentation on resources</a>
 * and <a href="https://junit-pioneer.org/docs/temp-directory/">temporary directories</a>.</p>
 *
 * @since 1.9.0
 * @see ResourceFactory
 * @see New
 * @see Shared
 */
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
		public Path get() {
			return tempDir;
		}

		@Override
		public void close() throws Exception {
			deleteRecursively(tempDir);
		}

		private static void deleteRecursively(Path tempDir) throws IOException {
			Files.walkFileTree(tempDir, PathDeleter.INSTANCE);
		}

	}

}
