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

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.PreconditionViolationException;

class JsonConverterProvider {

	private static final boolean jacksonPresent = isClassPresent("com.fasterxml.jackson.databind.ObjectMapper");

	static boolean isClassPresent(String className) {
		try {
			JsonConverterProvider.class.getClassLoader().loadClass(className);
			return true;
		}
		catch (Throwable e) {
			return false;
		}
	}

	static JsonConverter getJsonConverter(ExtensionContext context) {
		//TODO use context.getConfigurationParameter("junit.pioneer.json.converters") to pick a different converter
		// TODO perhaps even add converter to the @JsonFileSource
		// TODO perhaps a new annotation @JsonConverter for picking the converter
		if (jacksonPresent) {
			return JacksonJsonConverter.getConverter();
		}

		throw new PreconditionViolationException("There is no available Json parsing library");
	}

}
