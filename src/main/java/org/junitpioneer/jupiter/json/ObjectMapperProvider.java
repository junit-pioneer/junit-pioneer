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

import tools.jackson.core.json.JsonFactory;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Service interface for providing a custom {@link tools.jackson.databind.ObjectMapper} instance at runtime.
 * The default implementation doesn't register any additional Jackson modules.
 */
public interface ObjectMapperProvider {

	ObjectMapper get();

	default ObjectMapper getLenient() {
		var mapper = get();
		if (mapper instanceof JsonMapper) {
			return ((JsonMapper) mapper)
					.rebuild()
					.enable(JsonReadFeature.ALLOW_UNQUOTED_PROPERTY_NAMES)
					.enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
					.enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
					.enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
					.build();
		}
		if (!(mapper.tokenStreamFactory() instanceof JsonFactory)) {
			// Lenient parsing is a JSON-specific concept (JsonReadFeature); mappers backed by a
			// different format (e.g. XML, YAML, CBOR) have no equivalent, so fall back to `get()`.
			return mapper;
		}
		var factory = ((JsonFactory) mapper.tokenStreamFactory())
				.rebuild()
				.enable(JsonReadFeature.ALLOW_UNQUOTED_PROPERTY_NAMES)
				.enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
				.enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
				.enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
				.build();
		var builder = JsonMapper.builder(factory).addModules(mapper.registeredModules());
		// JsonMapper.builder() can't get feature flags from an arbitrary ObjectMapper directly,
		// so we re-apply the standard ones by hand.
		// This won't carry over anything outside those features.
		for (MapperFeature feature : MapperFeature.values())
			builder.configure(feature, mapper.isEnabled(feature));
		for (SerializationFeature feature : SerializationFeature.values())
			builder.configure(feature, mapper.isEnabled(feature));
		for (DeserializationFeature feature : DeserializationFeature.values())
			builder.configure(feature, mapper.isEnabled(feature));
		return builder.build();
	}

	String id();

}
