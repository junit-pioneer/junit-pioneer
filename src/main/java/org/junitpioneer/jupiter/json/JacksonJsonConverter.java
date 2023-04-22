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
import java.util.List;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junitpioneer.internal.PioneerPropertyUtils;

/**
 * A {@link JsonConverter} using Jackson 2 {@link ObjectMapper} to perform the conversion
 */
class JacksonJsonConverter implements JsonConverter {

	private static final String PROPERTY_REGISTRATION = "junit-pioneer.jackson.modules.registration";
	private static final String PROPERTY_LIST = "junit-pioneer.jackson.modules.list";
	private static final String ALL = "all";
	private static final String NONE = "none";
	private static final String LIST = "list";
	private static final JacksonJsonConverter INSTANCE = new JacksonJsonConverter(new ObjectMapper());

	private final ObjectMapper objectMapper;
	private final ObjectMapper lenientObjectMapper;

	private String type;

	JacksonJsonConverter(ObjectMapper objectMapper) {
		this(objectMapper, null);
	}

	/**
	 * This constructor was intended to be used for testing purposes.
	 * It could also be used to let users inject module registration key programmatically.
	 *
	 * @param objectMapper the object mapper
	 * @param type the keyword for module registration, must be "all", "list" or "none"
	 *				(or {@code null}, if we read it from the property file)
	 */
	JacksonJsonConverter(ObjectMapper objectMapper, String type) {
		this.type = type;
		this.objectMapper = objectMapper;
		configure(this.objectMapper);
		this.lenientObjectMapper = this.objectMapper.copy();
		this.lenientObjectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		this.lenientObjectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		this.lenientObjectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
	}

	private void configure(ObjectMapper objectMapper) {
		if (type == null)
			type = PioneerPropertyUtils.property(PROPERTY_REGISTRATION).orElse(NONE);
		if (!type.equals(NONE)) {
			switch (type) {
				case ALL:
					objectMapper.findAndRegisterModules();
					return;
				case LIST:
					List<String> modules = PioneerPropertyUtils.list(PROPERTY_LIST);
					modules.forEach(registerModule(objectMapper));
					return;
				default:
					throw new ExtensionConfigurationException(type + " is not a valid value for "
							+ PROPERTY_REGISTRATION + "Must be one of:" + String.join(", ", ALL, LIST, NONE) + ".");
			}
		}
	}

	private static Consumer<String> registerModule(ObjectMapper objectMapper) {
		return name -> {
			try {
				@SuppressWarnings("unchecked")
				Class<Module> module = (Class<Module>) Class.forName(name);
				objectMapper.registerModule(module.getDeclaredConstructor().newInstance());
			}
			catch (Exception exception) {
				throw new JacksonModuleNotFoundException("Failed loading jackson module " + name, exception);
			}
		};
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
