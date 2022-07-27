/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.resource.testcase;

import static org.junitpioneer.jupiter.resource.Scope.SOURCE_FILE;

import java.nio.file.Path;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.resource.Shared;
import org.junitpioneer.jupiter.resource.TemporaryDirectory;

public class SecondSingleTopLevelTestMethodWithSourceFileScopedTempDirParameter {

	public static Path recordedOuterPath;
	public static Path recordedInnerPath;

	public static class OuterTestCases {

		@Test
		void theTest(
				@Shared(factory = TemporaryDirectory.class, name = "some-name", scope = SOURCE_FILE) Path tempDir) {
			recordedOuterPath = tempDir;
		}

		@Nested
		class InnerTestCases {

			@Test
			void theTest(
					@Shared(factory = TemporaryDirectory.class, name = "some-name", scope = SOURCE_FILE) Path tempDir) {
				recordedInnerPath = tempDir;
			}

		}

	}

}
