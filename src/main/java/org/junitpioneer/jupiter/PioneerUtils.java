/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static org.junit.platform.commons.util.ReflectionUtils.findMethod;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Pioneer-internal utility class.
 *
 * @see PioneerAnnotationUtils
 */
class PioneerUtils {

	private PioneerUtils() {
		// private constructor to prevent instantiation of utility class
	}

	/**
	 * A {@link Collectors#toSet() toSet} collector that throws an {@link IllegalStateException}
	 * on duplicate elements (according to {@link Object#equals(Object) equals}).
	 */
	public static <T> Collector<T, Set<T>, Set<T>> distinctToSet() {
		return Collector.of(HashSet::new, PioneerUtils::addButThrowIfDuplicate, (left, right) -> {
			right.forEach(element -> addButThrowIfDuplicate(left, element));
			return left;
		});
	}

	private static <T> void addButThrowIfDuplicate(Set<T> set, T element) {
		boolean newElement = set.add(element);
		if (!newElement) {
			throw new IllegalStateException("Duplicate element '" + element + "'.");
		}
	}

	/**
	 * Find the first {@link Method} of the supplied class or interface that
	 * meets the specified criteria, beginning with the specified class or
	 * interface and traversing its enclosing classes until such a method is
	 * found or the top level class is reached.
	 *
	 * <p>The algorithm does not search for methods in {@link java.lang.Object}.
	 *
	 * @param clazz the class or interface in which to find the method; never {@code null}
	 * @param methodName the name of the method to find; never {@code null} or empty
	 * @param parameterTypes the types of parameters accepted by the method, if any;
	 * never {@code null}
	 * @return an {@code Optional} containing the method found; never {@code null}
	 * but potentially empty if no such method could be found
	 * @see org.junit.platform.commons.util.ReflectionUtils#findMethod(Class, String, Class...)
	 */
	public static Optional<Method> findMethodCurrentOrEnclosing(Class<?> clazz, String methodName,
			Class<?>... parameterTypes) {
		Class<?> current = clazz;
		Optional<Method> method;
		do {
			// null checking done by ReflectionUtils.findMethod
			method = findMethod(current, methodName, parameterTypes);
			current = current.getEnclosingClass();
		} while (!method.isPresent() && current != null);
		return method;
	}

}
