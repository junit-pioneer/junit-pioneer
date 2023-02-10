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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junitpioneer.internal.PropertyUtils;

/**
 * A {@link JsonConverter} using Jackson 2 {@link ObjectMapper} to perform the conversion
 */
class JacksonJsonConverter implements JsonConverter {

	private static final String PROPERTY_PREFIX = "junitpioneer.jackson.modules";
	private static final JacksonJsonConverter INSTANCE = new JacksonJsonConverter(new ObjectMapper());

	private final ObjectMapper objectMapper;
	private final ObjectMapper lenientObjectMapper;

	JacksonJsonConverter(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		configure(this.objectMapper);
		this.lenientObjectMapper = this.objectMapper.copy();
		this.lenientObjectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		this.lenientObjectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		this.lenientObjectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
	}

	/**
	 * Configure the passed objectMapper based on the properties available.
	 *
	 * @param objectMapper The mapper to configure
	 * */
	private void configure(ObjectMapper objectMapper) {
		String type = PropertyUtils.property(PROPERTY_PREFIX + ".registration");
		if (type != null && type != "none")  {
			switch (type) {
				case "all":
					objectMapper.findAndRegisterModules();
					return;
				case "list":
					List<String> modules = PropertyUtils.list(PROPERTY_PREFIX + ".list");
					if (modules == null) {
						return;
					}
					modules.forEach(name -> {
						try {
							@SuppressWarnings("unchecked")
							Class<Module> module = (Class<Module>) Class.forName(name);
							objectMapper.registerModule( module.getDeclaredConstructor().newInstance());
						} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
						throw new RuntimeException("Failed loading jackson module " + name, e);
						}
					});
					return;
				default:
					throw new RuntimeException(type + " is not a valid value for " + PROPERTY_PREFIX +".registration. Must be either all, list or none.");
			}
		}
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

	static JacksonJsonConverter getConverter() {
		return INSTANCE;
	}

}
