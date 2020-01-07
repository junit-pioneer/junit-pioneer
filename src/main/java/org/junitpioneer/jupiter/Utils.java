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

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

/**
 * Pioneer-internal utility class.
 */
class Utils {

	private Utils() {
		// private constructor to prevent instantiation of utility class
	}

	/**
	 * Determines whether an annotation of any of the specified {@code annotationTypes}
	 * is either <em>present</em> or <em>meta-present</em> on the test method belonging
	 * to the specified {@code context}.
	 */
	public static boolean annotationPresentOnTestMethod(ExtensionContext context,
			Class<? extends Annotation>... annotationTypes) {
		//@formatter:off
		return context.getTestMethod()
				.map(testMethod -> Stream
						.of(annotationTypes)
						.anyMatch(annotationType -> AnnotationSupport.isAnnotated(testMethod, annotationType)))
				.orElse(false);
		//@formatter:on
	}

	/**
	 * A {@link Collectors#toSet() toSet} collector that throws an {@link IllegalStateException}
	 * on duplicate elements (according to {@link Object#equals(Object) equals}).
	 */
	public static <T> Collector<T, Set<T>, Set<T>> distinctToSet() {
		return Collector.of(HashSet::new, (set, element) -> addButThrowIfDuplicate(set, element), (left, right) -> {
			right.forEach(element -> {
				addButThrowIfDuplicate(right, element);
			});
			return left;
		});
	}

	private static <T> void addButThrowIfDuplicate(Set<T> right, T element) {
		boolean newElement = right.add(element);
		if (!newElement) {
			throw new IllegalStateException("Duplicate element '" + element + "'.");
		}
	}

}
