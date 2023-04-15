/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * Utility class to read a property file.
 */
public final class PioneerPropertyUtils {

	private static final String PROPERTY_FILE_NAME = "junit-pioneer.properties";
	private static final Properties properties = new Properties();

	static {
		URL url = Thread.currentThread().getContextClassLoader().getResource(PROPERTY_FILE_NAME);
		if (url != null) {
			try {
				URLConnection configConnection = url.openConnection();
				try (InputStream inputStream = configConnection.getInputStream()) {
					properties.load(inputStream);
				}
			}
			catch (IOException e) {
				// properties not found
			}
		}

	}

	private PioneerPropertyUtils() {
		// private constructor to prevent instantiation of utility class
	}

	/**
	 * Read a list of properties by name. The list items must be delimited with a semicolon.
	 *
	 * @return The list of properties or an empty list if the property isn't found.
	 */
	public static List<String> list(String name) {
		PioneerPreconditions.notBlank(name, "key must not be null or blank");
		String value = properties.getProperty(name);
		if (value == null) {
			return List.of();
		}
		return List.of(value.split(","));
	}

	/**
	 * Get a property by name.
	 *
	 * @return The property or null if not specified.
	 */
	public static Optional<String> property(String name) {
		PioneerPreconditions.notBlank(name, "key must not be null or blank");
		return Optional.ofNullable(properties.getProperty(name));
	}

}
