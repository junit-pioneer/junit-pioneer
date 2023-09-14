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

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.ServiceLoader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junitpioneer.internal.PioneerPreconditions;

/**
 * A {@link JsonConverter} using Jackson 2 {@link ObjectMapper} to perform the conversion
 */
class JacksonJsonConverter implements JsonConverter {

	private static final Map<String, ObjectMapperProvider> OBJECT_MAPPERS = loadObjectMappers();

	private final ObjectMapper objectMapper;

	private final ObjectMapper lenientObjectMapper;

	JacksonJsonConverter(ObjectMapperProvider provider) {
		PioneerPreconditions.notNull(provider, "Could not find custom object mapper.");
		this.objectMapper = provider.get();
		this.lenientObjectMapper = provider.getLenient();
	}

	private static Map<String, ObjectMapperProvider> loadObjectMappers() {
		return ServiceLoader
				.load(ObjectMapperProvider.class)
				.stream()
				.map(ServiceLoader.Provider::get)
				.collect(toMap(ObjectMapperProvider::id, identity()));
	}

	@Override
	public Node toNode(InputStream stream) {
		try {
			JsonNode jsonNode = objectMapper.readTree(stream);
			return new JacksonNode(objectMapper, jsonNode);
		}
		catch (IOException e) {
			throw new UncheckedIOException("Failed to read stream", e);
		}
	}

	@Override
	public Node toNode(String value, boolean lenient) {
		try {
			JsonNode jsonNode = getObjectMapper(lenient).readTree(value);
			return new JacksonNode(getObjectMapper(false), jsonNode);
		}
		catch (IOException e) {
			throw new UncheckedIOException("Failed to read value", e);
		}
	}

	private ObjectMapper getObjectMapper(boolean lenient) {
		return lenient ? lenientObjectMapper : objectMapper;
	}

	static JacksonJsonConverter getConverter(String objectMapperId) {
		return new JacksonJsonConverter(OBJECT_MAPPERS.get(objectMapperId));
	}

}
