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
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

import static java.util.stream.Collectors.toList;

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
	@Deprecated
	public static boolean annotationPresentOnTestMethod(ExtensionContext context,
			Class<? extends Annotation>... annotationTypes) {
		return context
				.getTestMethod()
				.map(testMethod -> Stream
						.of(annotationTypes)
						.anyMatch(annotationType -> AnnotationSupport.isAnnotated(testMethod, annotationType)))
				.orElse(false);
	}

	/**
	 * Determines whether an annotation of any of the specified {@code annotationTypes}
	 * is either <em>present</em> or <em>meta-present</em> on the test element (method or
	 * class) or any enclosing class belonging to the specified {@code context}.
	 */
	public static boolean annotationPresent(ExtensionContext context,
			Class<? extends Annotation>... annotationTypes) {
		return Stream.of(annotationTypes)
				.map(annotationType -> findAnnotation(context, annotationType))
				.anyMatch(Optional::isPresent);
	}

	/**
	 * Returns the specified annotation if it is either <em>present</em> or <em>meta-present</em>
	 * on the test element (method or class) or any enclosing class belonging to the specified
	 * {@code context}.
	 */
	public static <A extends Annotation> Optional<A> findAnnotation(ExtensionContext context, Class<A> annotationType) {
		Optional<A> methodAnnotation = AnnotationSupport.findAnnotation(context.getTestMethod(), annotationType);
		if (methodAnnotation.isPresent())
			return methodAnnotation;

		return findAnnotationOnClass(context.getTestClass(), annotationType);
	}

	private static <A extends Annotation> Optional<A> findAnnotationOnClass(Optional<Class<?>> type, Class<A> annotationType) {
		if (!type.isPresent())
			return Optional.empty();

		Optional<A> annotation = AnnotationSupport.findAnnotation(type, annotationType);
		if (annotation.isPresent())
			return annotation;

		return findAnnotationOnClass(type.map(Class::getEnclosingClass), annotationType);
	}

	/**
	 * Returns the specified repeatable annotation if it either is either <em>present</em> or
	 * <em>meta-present</em> on the test method belonging to the specified {@code context}.
	 */
	public static <A extends Annotation> Stream<A> findRepeatableAnnotation(ExtensionContext context,
			Class<A> annotationType) {
		return Stream
				.of(context.getElement(), context.getTestClass().map(Class::getEnclosingClass))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.flatMap(el -> AnnotationSupport.findRepeatableAnnotations(el, annotationType).stream());
	}

	/**
	 * A {@link Collectors#toSet() toSet} collector that throws an {@link IllegalStateException}
	 * on duplicate elements (according to {@link Object#equals(Object) equals}).
	 */
	public static <T> Collector<T, Set<T>, Set<T>> distinctToSet() {
		return Collector.of(HashSet::new, Utils::addButThrowIfDuplicate, (left, right) -> {
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
