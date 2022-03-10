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
		if (jacksonPresent) {
			return JacksonJsonConverter.getConverter();
		}

		throw new NoJsonParserConfiguredException();
	}

}
