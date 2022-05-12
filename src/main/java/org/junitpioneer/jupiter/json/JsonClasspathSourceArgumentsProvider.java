/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.json;

import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junitpioneer.internal.PioneerPreconditions;

/**
 * Provides arguments from JSON files specified with {@link JsonClasspathSource}.
 */
class JsonClasspathSourceArgumentsProvider extends AbstractJsonSourceBasedArgumentsProvider<JsonClasspathSource> {

	// the reading of the resources / files is heavily inspired by Jupiter's CsvFileArgumentsProvider

	@Override
	public void accept(JsonClasspathSource jsonSource) {
		Stream<Source> resources = Arrays
				.stream(jsonSource.value())
				.map(JsonClasspathSourceArgumentsProvider::classpathResource);

		accept(resources.collect(Collectors.toList()), jsonSource.data());
	}

	private static Source classpathResource(String resource) {
		return context -> {
			PioneerPreconditions.notBlank(resource, "Classpath resource must not be null or blank");
			InputStream stream = context.getRequiredTestClass().getClassLoader().getResourceAsStream(resource);
			PioneerPreconditions.notNull(stream, "Classpath resource [" + resource + "] does not exist");
			return stream;
		};
	}

}
