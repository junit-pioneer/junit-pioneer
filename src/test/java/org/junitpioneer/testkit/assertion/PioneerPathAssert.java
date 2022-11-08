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
import static java.util.Collections.singletonList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.assertj.core.api.PathAssert;

public class PioneerPathAssert extends PathAssert {

	public static PioneerPathAssert assertThatPath(Path actual) {
		return new PioneerPathAssert(actual);
	}

	private PioneerPathAssert(Path path) {
		super(path);
	}

	public PioneerPathAssert canAddTextFile() {
		canAddTextFileInternal();
		return this;
	}

	public PioneerPathAssert canReadTextFile() {
		Path textFile = canAddTextFileInternal();

		String expectedText = "some-text";
		try {
			Files.write(textFile, singletonList(expectedText));
		}
		catch (IOException e) {
			throw failure("Cannot write to a text file");
		}

		String actualText;
		try {
			actualText = new String(Files.readAllBytes(textFile), UTF_8).trim();
		}
		catch (IOException e) {
			throw failure("Cannot read from a text file");
		}

		if (!Objects.equals(actualText, expectedText)) {
			throw failureWithActualExpected(actualText, expectedText,
				"Text file expected to contain <%s>, but was <%s>", expectedText, actualText);
		}

		return this;
	}

	private Path canAddTextFileInternal() {
		isNotNull();

		Path textFile;
		try {
			textFile = Files.createTempFile(actual, "some-text-file", ".txt");
		}
		catch (IOException e) {
			throw failure("Cannot add a text file");
		}
		return textFile;
	}

}
