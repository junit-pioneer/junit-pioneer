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

import static java.util.stream.Collectors.joining;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.stream.IntStream;

import org.junit.jupiter.api.extension.ExtensionConfigurationException;

public final class TestNameFormatter {

	// This code is a slightly refactored variant of the JUnit Jupiter class
	// `org.junit.jupiter.params.ParameterizedTestNameFormatter` (from v5.8.2).

	public static final String DISPLAY_NAME_PLACEHOLDER = "{displayName}";
	public static final String INDEX_PLACEHOLDER = "{index}";
	public static final String ARGUMENTS_PLACEHOLDER = "{arguments}";

	private final String pattern;
	private final String displayName;
	private final Class<?> forClass;

	public TestNameFormatter(String pattern, String displayName, Class<?> forClass) {
		this.pattern = pattern;
		this.displayName = displayName;
		this.forClass = forClass;
	}

	public String format(int invocationIndex, Object... arguments) {
		try {
			return formatSafely(invocationIndex, arguments);
		}
		catch (Exception ex) {
			String message = "The display name pattern defined for the " + forClass.getName() + " is invalid. "
					+ "See nested exception for further details.";
			throw new ExtensionConfigurationException(message, ex);
		}
	}

	private String formatSafely(int invocationIndex, Object[] arguments) {
		String messageFormatPattern = prepareMessageFormatPattern(invocationIndex, arguments);
		MessageFormat format = new MessageFormat(messageFormatPattern);
		Object[] readableArguments = makeReadable(arguments);
		return format.format(readableArguments);
	}

	private String prepareMessageFormatPattern(int invocationIndex, Object[] arguments) {
		String result = pattern
				.replace(DISPLAY_NAME_PLACEHOLDER, this.displayName)
				.replace(INDEX_PLACEHOLDER, String.valueOf(invocationIndex));

		if (result.contains(ARGUMENTS_PLACEHOLDER)) {
			String replacement = IntStream
					.range(0, arguments.length)
					.mapToObj(index -> "{" + index + "}")
					.collect(joining(", "));
			result = result.replace(ARGUMENTS_PLACEHOLDER, replacement);
		}

		return result;
	}

	private Object[] makeReadable(Object[] arguments) {
		Object[] result = Arrays.copyOf(arguments, arguments.length, Object[].class);
		for (int i = 0; i < result.length; i++) {
			result[i] = PioneerUtils.nullSafeToString(arguments[i]);
		}
		return result;
	}

}
