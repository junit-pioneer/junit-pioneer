/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.resource;

import static org.junitpioneer.jupiter.resource.Assertions.assertCanAddAndReadTextFile;
import static org.junitpioneer.jupiter.resource.Assertions.assertReadableWriteableTemporaryDirectory;
import static org.junitpioneer.jupiter.resource.Scope.SOURCE_FILE;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class FirstSingleTopLevelTestMethodWithSourceFileScopedTempDirParameter {

	static class OnlyTestCases {

		static Path recordedPath;

		@Test
		void theTest(
				@Shared(factory = TemporaryDirectory.class, name = "some-name", scope = SOURCE_FILE) Path tempDir) {
			assertReadableWriteableTemporaryDirectory(tempDir);
			assertCanAddAndReadTextFile(tempDir);

			recordedPath = tempDir;
		}

	}

}
