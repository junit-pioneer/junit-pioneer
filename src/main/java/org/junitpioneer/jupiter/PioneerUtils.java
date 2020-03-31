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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

/**
 * Pioneer-internal utility class.
 */
class PioneerUtils {

	private PioneerUtils() {
		// private constructor to prevent instantiation of utility class
	}

	/**
	 * Determines whether an annotation of any of the specified {@code annotationTypes}
	 * is either <em>present</em>, <em>indirectly present</em>, or <em>meta-present</em>
	 * on the test element (method or class) or any enclosing class belonging to the
	 * specified {@code context}.
	 */
	public static boolean annotationsPresent(ExtensionContext context, Class<? extends Annotation>... annotationTypes) {
		return Stream
				.of(annotationTypes)
				.map(annotationType -> findClosestAnnotation(context, annotationType))
				.anyMatch(Optional::isPresent);
	}

	/**
	 * Determines whether an annotation of any of the specified repeatable
	 * {@code annotationTypes} is either <em>present</em>, <em>indirectly present</em>,
	 * or <em>meta-present</em> on the test element (method or class) or any enclosing
	 * class belonging to the specified {@code context}.
	 */
	public static boolean repeatableAnnotationsPresent(ExtensionContext context,
			Class<? extends Annotation>... annotationTypes) {
		return Stream
				.of(annotationTypes)
				.flatMap(annotationType -> findClosestRepeatableAnnotation(context, annotationType))
				.iterator()
				.hasNext();
	}

	/**
	 * Returns the specified annotation if it is either <em>present</em>, <em>indirectly present</em>,
	 * or <em>meta-present</em> on the test element (method or class) or any enclosing class belonging
	 * to the specified {@code context}. If the annotation is present on more than one enclosing type,
	 * the closest one is returned.
	 */
	public static <A extends Annotation> Optional<A> findClosestAnnotation(ExtensionContext context,
			Class<A> annotationType) {
		return findOnAnything(context, annotationType, false, true).findFirst();
	}

	/**
	 * Returns the specified annotation if it is either <em>present</em>, <em>indirectly present</em>,
	 * or <em>meta-present</em> on the test element (method or class) or any enclosing class belonging
	 * to the specified {@code context}. If the annotation is present on more than one enclosing type,
	 * all instances are returned.
	 */
	public static <A extends Annotation> Stream<A> findAllAnnotations(ExtensionContext context,
			Class<A> annotationType) {
		return findOnAnything(context, annotationType, false, true);
	}

	/**
	 * Returns the specified repeatable annotation if it is either <em>present</em>, <em>indirectly present</em>,
	 * or <em>meta-present</em> on the test element (method or class) or any enclosing class belonging
	 * to the specified {@code context}. If the annotation is present on more than one enclosing type,
	 * the instances on the closest one are returned.
	 */
	public static <A extends Annotation> Stream<A> findClosestRepeatableAnnotation(ExtensionContext context,
			Class<A> annotationType) {
		return findOnAnything(context, annotationType, true, true);
	}

	/**
	 * Returns the specified repeatable annotation if it is either <em>present</em>, <em>indirectly present</em>,
	 * or <em>meta-present</em> on the test element (method or class) or any enclosing class belonging
	 * to the specified {@code context}. If the annotation is present on more than one enclosing type,
	 * all instances are returned.
	 */
	public static <A extends Annotation> Stream<A> findAllRepeatableAnnotations(ExtensionContext context,
			Class<A> annotationType) {
		return findOnAnything(context, annotationType, true, true);
	}

	static <A extends Annotation> Stream<A> findOnAnything(ExtensionContext context, Class<A> annotationType,
			boolean repeatable, boolean stackable) {
		List<A> onMethod = context
				.getTestMethod()
				.map(method -> findOnElement(method, annotationType, repeatable))
				.orElse(Collections.emptyList());
		if (stackable && !onMethod.isEmpty())
			return onMethod.stream();
		Stream<A> onClass = findOnOuterClasses(context.getTestClass(), annotationType, repeatable, stackable);

		return Stream.concat(onMethod.stream(), onClass);
	}

	private static <A extends Annotation> List<A> findOnElement(AnnotatedElement element, Class<A> annotationType,
			boolean repeatable) {
		if (repeatable)
			return AnnotationSupport.findRepeatableAnnotations(element, annotationType);
		else
			return AnnotationSupport
					.findAnnotation(element, annotationType)
					.map(Collections::singletonList)
					.orElse(Collections.emptyList());
	}

	private static <A extends Annotation> Stream<A> findOnOuterClasses(Optional<Class<?>> type, Class<A> annotationType,
			boolean repeatable, boolean stackable) {
		if (!type.isPresent())
			return Stream.empty();

		List<A> onThisClass = findOnElement(type.get(), annotationType, repeatable);
		if (stackable && !onThisClass.isEmpty())
			return onThisClass.stream();

		Stream<A> onParentClass = findOnOuterClasses(type.map(Class::getEnclosingClass), annotationType, repeatable,
			stackable);
		return Stream.concat(onThisClass.stream(), onParentClass);
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
