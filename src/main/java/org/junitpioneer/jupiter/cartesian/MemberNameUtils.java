/*
 * Copyright 2026 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.cartesian;

import java.util.Optional;

/**
 * Utility methods for parsing member references used by
 * {@link CartesianFactoryArgumentsProvider}.
 *
 * <p>A member reference has the form
 * {@code [className#]methodName[(parameters)]}, where the class name and
 * the parameter list are optional.
 */
class MemberNameUtils {

	// utility class no-arg constructor
	private MemberNameUtils() {
	}

	/**
	 * Returns the method name of the given member reference, if present.
	 *
	 * <p>Any parameter list (starting at the first {@code '('}) and any class
	 * name (up to and including the first {@code '#'}) are removed, and the
	 * remaining string is trimmed.
	 *
	 * <p>Examples:
	 * <blockquote><pre>
	 * extractMethodName("myMethod") returns Optional[myMethod]
	 * extractMethodName("MyClass#myMethod") returns Optional[myMethod]
	 * extractMethodName("MyClass#myMethod(int)") returns Optional[myMethod]
	 * extractMethodName("MyClass#") returns Optional.empty
	 * </pre></blockquote>
	 *
	 * @param memberName the member reference; may be {@code null}
	 * @return an {@code Optional} containing the method name, or an empty
	 *         {@code Optional} if {@code memberName} is {@code null} or has a
	 *         blank method name
	 */
	public static Optional<String> extractMethodName(String memberName) {
		if (memberName == null) {
			return Optional.empty();
		}

		String name = memberName;
		if (name.contains("(")) {
			name = name.substring(0, name.indexOf('('));
		}

		if (name.contains("#")) {
			name = name.substring(name.indexOf('#') + 1);
		}

		String methodName = name.trim();
		return methodName.isEmpty() ? Optional.empty() : Optional.of(methodName);
	}

	/**
	 * Returns the class name of the given member reference, if present.
	 *
	 * <p>The class name is the part before the first {@code '#'}, trimmed.
	 *
	 * <p>Examples:
	 * <blockquote><pre>
	 * extractClassName("MyClass#myMethod") returns Optional[MyClass]
	 * extractClassName("com.example.MyClass#myMethod(int)") returns Optional[com.example.MyClass]
	 * extractClassName("myMethod") returns Optional.empty
	 * extractClassName("#myMethod") returns Optional.empty
	 * </pre></blockquote>
	 *
	 * @param memberName the member reference; may be {@code null}
	 * @return an {@code Optional} containing the class name, or an empty
	 *         {@code Optional} if {@code memberName} is {@code null},
	 *         contains no {@code '#'}, or has a blank class name
	 */
	public static Optional<String> extractClassName(String memberName) {
		if (memberName == null || !memberName.contains("#")) {
			return Optional.empty();
		}

		String className = memberName.substring(0, memberName.indexOf('#')).trim();
		return className.isEmpty() ? Optional.empty() : Optional.of(className);
	}

}
