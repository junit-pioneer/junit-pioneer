/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.displaynamegenerator;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayNameGenerator;

/**
 * <p>A class extending {@linkplain DisplayNameGenerator.Standard }.</p>
 *
 * <p>This extension handles method names with CamelCase, underscore and numbers.</p>
 *
 * <p>The aim is to simplify unit test display names. Instead of using this method annotation {@linkplain org.junit.jupiter.api.DisplayName },
 * we can just use this class annotation {@linkplain org.junit.jupiter.api.DisplayNameGeneration } and use that method annotation if needed.
 * </p>
 *
 * <p>This generator follows 3 rules:</p>
 *
 * <ul>
 *     <li>Each uppercase letter is turned into its lowercase value prepended by space.</li>
 *     <li>Each underscore is turned into space. Words bounded by underscores or just starting with underscore are not transformed. Usually these words represent classes, variables...</li>
 *     <li>Each number is prepended by space.</li>
 * </ul>
 * <p>
 * Usage example:
 *
 * <pre>
 *
 * {@code @DisplayNameGeneration(ReplaceCamelCaseAndUnderscoreAndNumber.class)}
 * class ExampleTest {}
 * </pre>
 *
 * @since 2.3.0
 * @see org.junit.jupiter.api.DisplayNameGenerator.Standard
 * @see <a href="https://junit-pioneer.org/docs/replace-camelcase-and-underscore-and-number">Usage example of ReplaceCamelCaseAndUnderscoreAndNumber</a>
 */
public class ReplaceCamelCaseAndUnderscoreAndNumber extends DisplayNameGenerator.Standard {

	public static final DisplayNameGenerator INSTANCE = new ReplaceCamelCaseAndUnderscoreAndNumber();

	private ReplaceCamelCaseAndUnderscoreAndNumber() {
	}

	@Override
	public String generateDisplayNameForMethod(List<Class<?>> enclosingInstanceTypes, Class<?> testClass, Method testMethod) {
		if (hasParameters(testMethod)) {
			return replaceCamelCaseAndUnderscoreAndNumber(testMethod.getName()) + " "
					+ DisplayNameGenerator.parameterTypesAsString(testMethod);
		}
		return replaceCamelCaseAndUnderscoreAndNumber(testMethod.getName());
	}

	private String replaceCamelCaseAndUnderscoreAndNumber(String input) {
		// Remove leading underscore(s)
		var sanitized = input.replaceAll("^_+(.*)$", "$1");
		List<String> list = new ArrayList<>();
		String[] split = sanitized.split("_");
		for (int i = 0; i < split.length; i++) {
			if (i % 2 == 1) {
				// If parity is odd, we are between two underscores, no formatting necessary
				list.add(split[i]);
			} else {
				list.add(formatCamelCase(split[i]));
			}
		}
		// Some cases can lead to double spaces - i.e.: underscore (closing) followed by capital letter
		var joined = String.join(" ", list).replaceAll("\\s{2}", " ");
		// Capitalize the first letter
		return joined.substring(0, 1).toUpperCase() + joined.substring(1);
	}

	private String formatCamelCase(String in) {
		return in.replaceAll("(\\d+)", " $1").replaceAll("([A-Z]+)", " $1").toLowerCase();
	}

	private boolean hasParameters(Method method) {
		return method.getParameterCount() > 0;
	}

}
