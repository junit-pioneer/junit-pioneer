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

import java.io.InputStream;
import java.util.Map;
import java.util.ServiceLoader;

import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junitpioneer.internal.PioneerPreconditions;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * A {@link JsonConverter} using Jackson 3 {@link ObjectMapper} to perform the conversion
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
		} catch (Exception e) {
			throw new ParameterResolutionException("Could not convert JSON to Node.", e);
		}
	}

	@Override
	public Node toNode(String value, boolean lenient) {
		try {
			JsonNode jsonNode = getObjectMapper(lenient).readTree(value);
			return new JacksonNode(getObjectMapper(false), jsonNode);
		} catch (Exception e) {
			throw new ParameterResolutionException("Could not convert JSON to Node.", e);
		}
	}

	private ObjectMapper getObjectMapper(boolean lenient) {
		return lenient ? lenientObjectMapper : objectMapper;
	}

	static JacksonJsonConverter getConverter(String objectMapperId) {
		return new JacksonJsonConverter(OBJECT_MAPPERS.get(objectMapperId));
	}

}
