/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.internal;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.platform.commons.support.AnnotationSupport;

/**
 * Pioneer-internal utility class to handle annotations.
 * DO NOT USE THIS CLASS - IT MAY CHANGE SIGNIFICANTLY IN ANY MINOR UPDATE.
 *
 * It uses the following terminology to describe annotations that are not
 * immediately present on an element:
 *
 * <ul>
 *     <li><em>indirectly present</em> if a supertype of the element is annotated</li>
 *     <li><em>meta-present</em> if an annotation that is present on the element is itself annotated</li>
 *     <li><em>enclosing-present</em> if an enclosing type (think opposite of
 *     		{@link org.junit.jupiter.api.Nested @Nested}) is annotated</li>
 * </ul>
 *
 * All of the above mechanisms apply recursively, meaning that, e.g., for an annotation to be
 * <em>meta-present</em> it can present on an annotation that is present on another annotation
 * that is present on the element.
 */
public class PioneerAnnotationUtils {

	private PioneerAnnotationUtils() {
		// private constructor to prevent instantiation of utility class
	}

	/**
	 * Determines whether an annotation of any of the specified {@code annotationTypes}
	 * is either <em>present</em>, <em>indirectly present</em>, <em>meta-present</em>, or
	 * <em>enclosing-present</em> on the test element (method or class) belonging to the
	 * specified {@code context}.
	 */
	public static boolean isAnyAnnotationPresent(ExtensionContext context,
			Class<? extends Annotation>... annotationTypes) {
		return Stream
				.of(annotationTypes)
				// to check for presence, we don't need all annotations - the closest ones suffice
				.map(annotationType -> findClosestEnclosingAnnotation(context, annotationType))
				.anyMatch(Optional::isPresent);
	}

	/**
	 * Determines whether an annotation of any of the specified repeatable {@code annotationTypes}
	 * is either <em>present</em>, <em>indirectly present</em>, <em>meta-present</em>, or
	 * <em>enclosing-present</em> on the test element (method or class) belonging to the specified
	 * {@code context}.
	 */
	public static boolean isAnyRepeatableAnnotationPresent(ExtensionContext context,
			Class<? extends Annotation>... annotationTypes) {
		return Stream
				.of(annotationTypes)
				.flatMap(annotationType -> findClosestEnclosingRepeatableAnnotations(context, annotationType))
				.iterator()
				.hasNext();
	}

	/**
	 * Returns the specified annotation if it is either <em>present</em>, <em>meta-present</em>,
	 * <em>enclosing-present</em>, or <em>indirectly present</em> on the test element (method or class) belonging
	 * to the specified {@code context}. If the annotations are present on more than one enclosing type,
	 * the closest ones are returned.
	 */
	public static <A extends Annotation> Optional<A> findClosestEnclosingAnnotation(ExtensionContext context,
			Class<A> annotationType) {
		return findAnnotations(context, annotationType, false, false).findFirst();
	}

	/**
	 * Returns the specified repeatable annotations if they are either <em>present</em>,
	 * <em>indirectly present</em>, <em>meta-present</em>, or <em>enclosing-present</em> on the test
	 * element (method or class) belonging to the specified {@code context}. If the annotations are
	 * present on more than one enclosing type, the instances on the closest one are returned.
	 */
	public static <A extends Annotation> Stream<A> findClosestEnclosingRepeatableAnnotations(ExtensionContext context,
			Class<A> annotationType) {
		return findAnnotations(context, annotationType, true, false);
	}

	/**
	 * Returns the specified annotations if they are either <em>present</em>, <em>indirectly present</em>,
	 * <em>meta-present</em>, or <em>enclosing-present</em> on the test element (method or class) belonging
	 * to the specified {@code context}. If the annotations are present on more than one enclosing type,
	 * all instances are returned.
	 */
	public static <A extends Annotation> Stream<A> findAllEnclosingAnnotations(ExtensionContext context,
			Class<A> annotationType) {
		return findAnnotations(context, annotationType, false, true);
	}

	/**
	 * Returns the specified repeatable annotations if they are either <em>present</em>,
	 * <em>indirectly present</em>, <em>meta-present</em>, or <em>enclosing-present</em> on the test
	 * element (method or class) belonging to the specified {@code context}. If the annotation is
	 * present on more than one enclosing type, all instances are returned.
	 */
	public static <A extends Annotation> Stream<A> findAllEnclosingRepeatableAnnotations(ExtensionContext context,
			Class<A> annotationType) {
		return findAnnotations(context, annotationType, true, true);
	}

	/**
	 * Returns the annotations <em>present</em> on the {@code AnnotatedElement}
	 * that are themselves annotated with the specified annotation. The meta-annotation can be <em>present</em>,
	 * <em>indirectly present</em>, or <em>meta-present</em>.
	 */
	public static <A extends Annotation> List<Annotation> findAnnotatedAnnotations(AnnotatedElement element,
			Class<A> annotation) {
		boolean isRepeatable = annotation.isAnnotationPresent(Repeatable.class);
		return Arrays
				.stream(element.getDeclaredAnnotations())
				// flatten @Repeatable aggregator annotations
				.flatMap(PioneerAnnotationUtils::flatten)
				.filter(a -> !(findOnType(a.annotationType(), annotation, isRepeatable, false).isEmpty()))
				.collect(Collectors.toList());
	}

	private static Stream<Annotation> flatten(Annotation annotation) {
		try {
			if (isContainerAnnotation(annotation)) {
				Method value = annotation.annotationType().getDeclaredMethod("value");
				Annotation[] invoke = (Annotation[]) value.invoke(annotation);
				return Stream.of(invoke).flatMap(PioneerAnnotationUtils::flatten);
			} else {
				return Stream.of(annotation);
			}
		}
		catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new RuntimeException("Failed to flatten annotation stream.", e); //NOSONAR
		}
	}

	public static boolean isContainerAnnotation(Annotation annotation) {
		// See https://docs.oracle.com/javase/specs/jls/se8/html/jls-9.html#jls-9.6.3
		try {
			Method value = annotation.annotationType().getDeclaredMethod("value");
			return value.getReturnType().isArray() && value.getReturnType().getComponentType().isAnnotation()
					&& isContainerAnnotationOf(annotation, value.getReturnType().getComponentType());
		}
		catch (NoSuchMethodException e) {
			return false;
		}
	}

	private static boolean isContainerAnnotationOf(Annotation potentialContainer, Class<?> potentialRepeatable) {
		Repeatable repeatable = potentialRepeatable.getAnnotation(Repeatable.class);
		return repeatable != null && repeatable.value().equals(potentialContainer.annotationType());
	}

	static <A extends Annotation> Stream<A> findAnnotations(ExtensionContext context, Class<A> annotationType,
			boolean findRepeated, boolean findAllEnclosing) {
		/*
		 * Implementation notes:
		 *
		 * This method starts with the specified element and, if not happy with the results (depends on the
		 * arguments and whether the annotation is present) kicks off a recursive search. The recursion steps
		 * through enclosing types (if required by the arguments, thus handling _enclosing-presence_) and
		 * eventually calls either `AnnotationSupport::findRepeatableAnnotations` or
		 * `AnnotationSupport::findAnnotation` (depending on arguments, thus handling the repeatable case).
		 * Both of these methods check for _meta-presence_ and _indirect presence_.
		 */
		List<A> onMethod = context
				.getTestMethod()
				.map(method -> findOnMethod(method, annotationType, findRepeated))
				.orElse(Collections.emptyList());
		if (!findAllEnclosing && !onMethod.isEmpty())
			return onMethod.stream();
		Stream<A> onClass = findOnOuterClasses(context.getTestClass(), annotationType, findRepeated, findAllEnclosing);

		return Stream.concat(onMethod.stream(), onClass);
	}

	private static <A extends Annotation> List<A> findOnMethod(Method element, Class<A> annotationType,
			boolean findRepeated) {
		if (findRepeated)
			return AnnotationSupport.findRepeatableAnnotations(element, annotationType);
		else
			return AnnotationSupport
					.findAnnotation(element, annotationType)
					.map(Collections::singletonList)
					.orElse(Collections.emptyList());
	}

	private static <A extends Annotation> List<A> findOnType(Class<?> element, Class<A> annotationType,
			boolean findRepeated, boolean findAllEnclosing) {
		if (element == null || element == Object.class)
			return Collections.emptyList();
		if (findRepeated)
			return AnnotationSupport.findRepeatableAnnotations(element, annotationType);

		List<A> onElement = AnnotationSupport
				.findAnnotation(element, annotationType)
				.map(Collections::singletonList)
				.orElse(Collections.emptyList());
		List<A> onInterfaces = Arrays
				.stream(element.getInterfaces())
				.flatMap(clazz -> findOnType(clazz, annotationType, false, findAllEnclosing).stream())
				.collect(Collectors.toList());
		if (!annotationType.isAnnotationPresent(Inherited.class)) {
			if (!findAllEnclosing)
				return onElement;
			else
				return Stream
						.of(onElement, onInterfaces)
						.flatMap(Collection::stream)
						.distinct()
						.collect(Collectors.toList());
		}
		List<A> onSuperclass = findOnType(element.getSuperclass(), annotationType, false, findAllEnclosing);
		return Stream
				.of(onElement, onInterfaces, onSuperclass)
				.flatMap(Collection::stream)
				.distinct()
				.collect(Collectors.toList());
	}

	private static <A extends Annotation> Stream<A> findOnOuterClasses(Optional<Class<?>> type, Class<A> annotationType,
			boolean findRepeated, boolean findAllEnclosing) {
		if (!type.isPresent())
			return Stream.empty();

		List<A> onThisClass = Arrays.asList(type.get().getAnnotationsByType(annotationType));
		if (!findAllEnclosing && !onThisClass.isEmpty())
			return onThisClass.stream();

		List<A> onClass = findOnType(type.get(), annotationType, findRepeated, findAllEnclosing);
		Stream<A> onParentClass = findOnOuterClasses(type.map(Class::getEnclosingClass), annotationType, findRepeated,
			findAllEnclosing);
		return Stream.concat(onClass.stream(), onParentClass);
	}

	public static List<? extends Annotation> findParameterArgumentsSources(Method testMethod) {
		return Arrays
				.stream(testMethod.getParameters())
				.map(parameter -> PioneerAnnotationUtils.findAnnotatedAnnotations(parameter, ArgumentsSource.class))
				.filter(list -> !list.isEmpty())
				.map(annotations -> annotations.get(0))
				.collect(Collectors.toList());
	}

}
