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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * Service interface for providing a custom {@link com.fasterxml.jackson.databind.ObjectMapper} instance at runtime.
 * The default implementation doesn't register any additional Jackson modules.
 *
 * @see com.fasterxml.jackson.databind.Module
 */
public interface ObjectMapperProvider {

	ObjectMapper get();

	default ObjectMapper getLenient() {
		var mapper = get();
		if (mapper instanceof JsonMapper) {
			return ((JsonMapper) mapper)
					.rebuild()
					.enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES)
					.enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
					.enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
					.enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
					.build();
		}
		return get()
				.copyWith(JsonFactory
						.builder()
						.enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES)
						.enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
						.enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
						.enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
						.build());
	}

	String id();

}
