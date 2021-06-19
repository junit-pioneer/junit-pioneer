/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A {@link JsonConverter} using Jackson 2 {@link ObjectMapper} to perform the conversion
 */
class JacksonJsonConverter implements JsonConverter {

	private static final JacksonJsonConverter INSTANCE = new JacksonJsonConverter(new ObjectMapper());

	private final ObjectMapper objectMapper;
	private final ObjectMapper lenientObjectMapper;

	JacksonJsonConverter(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		this.lenientObjectMapper = this.objectMapper.copy();
		this.lenientObjectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		this.lenientObjectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		this.lenientObjectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
	}

	@Override
	public Node toNode(InputStream stream) {
		try {
			ObjectMapper objectMapper = getObjectMapper(false);
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

	static JacksonJsonConverter getConverter() {
		return INSTANCE;
	}

}
