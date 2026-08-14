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

/** Package private class to handle string splitting/manipulation in
 * CartesianFactoryArgumentsProvider.java
 * */
class MemberNameUtils {

	// utility class no-arg constructor
	private MemberNameUtils() {
	}

	/** Extracts the method name from a String Reference. Examples are as follows:
	 *
	 * <p>Examples:
	 *   <ul>
	 * <li>"myMethod" => "myMethod"</li>
	 * <li>"MyClass#myMethod" => "myMethod"</li>
	 * <li>"MyClass#myMethod(int val)" => "myMethod" [regardless of parameters]</li>
	 *   </ul>
	 * </p>
	 *
	 * @param memberName  The String reference
	 * @return {@link String} The extracted method name
	 * @throws IllegalArgumentException  memberName cannot be null or empty
	 * */
	public static String extractMethodName(String memberName) {
		if (memberName == null || memberName.isBlank()) { // do not use isEmpty()
			throw new IllegalArgumentException("memberName reference cannot be null or empty");
		}

		String name = memberName;
		if (name.contains("(")) {
			name = name.substring(0, name.indexOf('('));
		}

		if (name.contains("#")) {
			name = name.substring(name.indexOf('#') + 1);
		}

		// check if the string is empty
		String trimmedName = name.trim();
		if (trimmedName.isEmpty()) {
			throw new IllegalArgumentException("memberName cannot be empty");
		}

		return trimmedName;
	}

	/** Extracts class name from String reference if present.
	 *
	 * @param memberName  The String reference
	 * @return an {@code Optional<String>} The extracted class name, or empty if absent
	 * @throws IllegalArgumentException  If memberName is null or not present
	 * */
	public static Optional<String> extractClassName(String memberName) {
		if (memberName == null || !memberName.contains("#")) {
			return Optional.empty();
		}

		String className = memberName.substring(0, memberName.indexOf('#')).trim();
		return className.isEmpty() ? Optional.empty() : Optional.of(className);
	}

}
