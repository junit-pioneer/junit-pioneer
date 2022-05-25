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

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junitpioneer.jupiter.resource.MorePaths.rootTempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class Assertions {

	static void assertCanAddAndReadTextFile(Path tempDir) {
		try {
			Path testFile = Files.createTempFile(tempDir, "some-test-file", ".txt");
			Files.write(testFile, singletonList("some-text"));
			assertThat(Files.readAllLines(testFile)).containsExactly("some-text");
		}
		catch (IOException e) {
			fail(e);
		}
	}

	static void assertReadableWriteableTemporaryDirectory(Path tempDir) {
		assertThat(tempDir).startsWith(rootTempDir()).isReadable().isWritable();
	}

	static void assertEmptyReadableWriteableTemporaryDirectory(Path tempDir) {
		assertThat(tempDir).isEmptyDirectory().startsWith(rootTempDir()).isReadable().isWritable();
	}

}
