/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.json;

import static java.util.stream.Collectors.toUnmodifiableList;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.platform.commons.PreconditionViolationException;
import org.junitpioneer.internal.PioneerPreconditions;

/**
 * Provides arguments from JSON files specified with {@link JsonFileSource}.
 */
class JsonFileSourceArgumentsProvider extends AbstractJsonSourceBasedArgumentsProvider<JsonFileSource> {

	// the reading of the resources / files is heavily inspired by Jupiter's CsvFileArgumentsProvider

	@Override
	public void accept(JsonFileSource jsonSource) {
		Stream<Source> files = Arrays.stream(jsonSource.value()).map(JsonFileSourceArgumentsProvider::fileResource);
		accept(files.collect(toUnmodifiableList()), jsonSource.data());
	}

	private static Source fileResource(String file) {
		return context -> {
			PioneerPreconditions.notBlank(file, "File must not be null or blank");
			Path filePath = Paths.get(file);
			if (!Files.exists(filePath))
				throw new PreconditionViolationException("File does not exist: " + file);
			try {
				return Files.newInputStream(filePath);
			}
			catch (IOException e) {
				throw new UncheckedIOException("Failed to read file " + file, e);
			}
		};
	}

}
