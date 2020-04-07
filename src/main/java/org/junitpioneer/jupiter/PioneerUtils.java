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

import java.util.HashSet;
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

}
