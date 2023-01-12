/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.testkit.assertion;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import org.assertj.core.api.PathAssert;

public class PioneerPathAssert extends PathAssert {

	PioneerPathAssert(Path path) {
		super(path);
	}

	public PioneerPathAssert canReadAndWriteFile() {
		isNotNull();

		Path textFile;
		try {
			textFile = Files.createTempFile(actual, "some-text-file", ".txt");
		}
		catch (IOException e1) {
			throw failure("Cannot create a file");
		}

		String expectedText = "some-text";
		try {
			Files.write(textFile, List.of(expectedText));
		}
		catch (IOException e) {
			throw failure("Cannot write to a file");
		}

		String actualText;
		try {
			actualText = new String(Files.readAllBytes(textFile), UTF_8).trim();
		}
		catch (IOException e) {
			throw failure("Cannot read from a file");
		}

		if (!Objects.equals(actualText, expectedText)) {
			throw failureWithActualExpected(actualText, expectedText, "File expected to contain <%s>, but was <%s>",
				expectedText, actualText);
		}

		return this;
	}

}
