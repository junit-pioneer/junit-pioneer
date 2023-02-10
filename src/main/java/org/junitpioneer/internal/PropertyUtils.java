/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package org.junitpioneer.internal;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Utils to read a property file.
 * */
public final class PropertyUtils {

	private static final String PROPERTY_FILE_NAME = "junitpioneer.properties";
	private static final Properties properties = new Properties();

	static {
		try {
			FileReader reader = new FileReader(PROPERTY_FILE_NAME);
			properties.load(reader);
		} catch (IOException e) {
			// properties not found
		}
	}

	private PropertyUtils() {

	}

	/**
	 * Read a list of properties by name. The list items must be delimited with a semicolon.
	 *
	 * @return The list or null if the property isn't found.
	 * */
	public static List<String> list(String name) {
		String value = properties.getProperty(name);
		if (value == null) {
			return null;
		}
		return Arrays.asList(value.split(","));
	}

	/** Get a property by name.
	 * @return The property or null if not specified.
	 * */
	public static String property(String name) {
		return properties.getProperty(name);
	}

}
