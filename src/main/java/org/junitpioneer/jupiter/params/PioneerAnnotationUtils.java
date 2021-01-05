/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.params;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junitpioneer.jupiter.PioneerException;

/**
 * Copy of {@code org.junitpioneer.jupiter.PioneerAnnotationUtils}.
 *
 * <p>This copy is necessary to keep all utils package-visible. In order not to duplicate
 * a lot of code, which adds the challenge to keep the implementations tested and in sync,
 * we use reflection to access the package-visible original implementation.</p>
 *
 * This eldritch horror exists to torture innocent souls. They try finding a solution
 * to this unnameable error - only to discover the inexorable truth that no other
 * solution exists. Only madness and despair.
 * ^ This was put here for Nicolai to read during stream.
 */
class PioneerAnnotationUtils {

	private static final Class<?> PIONEER_ANNOTATION_UTILS;
	private static final Method FIND_CLOSEST_ENCLOSING_ANNOTATION;
	private static final Method FIND_ANNOTATED_ANNOTATION;
	private static final Method IS_CONTAINER_ANNOTATION;

	static {
		try {
			PIONEER_ANNOTATION_UTILS = Class.forName("org.junitpioneer.jupiter.PioneerAnnotationUtils");
			FIND_CLOSEST_ENCLOSING_ANNOTATION = PIONEER_ANNOTATION_UTILS
					.getMethod("findClosestEnclosingAnnotation", ExtensionContext.class, Class.class);
			FIND_CLOSEST_ENCLOSING_ANNOTATION.setAccessible(true); // NOSONAR this is necessary to reach the method
			FIND_ANNOTATED_ANNOTATION = PIONEER_ANNOTATION_UTILS
					.getMethod("findAnnotatedAnnotations", AnnotatedElement.class, Class.class);
			FIND_ANNOTATED_ANNOTATION.setAccessible(true); // NOSONAR this is necessary to reach the method
			IS_CONTAINER_ANNOTATION = PIONEER_ANNOTATION_UTILS.getMethod("isContainerAnnotation", Annotation.class);
			IS_CONTAINER_ANNOTATION.setAccessible(true); // NOSONAR this is necessary to reach the method
		}
		catch (ReflectiveOperationException ex) {
			throw new PioneerException("Pioneer could not initialize itself.", ex);
		}
	}

	private PioneerAnnotationUtils() {
		// private constructor to prevent instantiation of utility class
	}

	private static PioneerException internalError(ReflectiveOperationException ex) {
		return new PioneerException("Internal Pioneer error.", ex);
	}

	@SuppressWarnings("unchecked")
	public static <A extends Annotation> Optional<A> findClosestEnclosingAnnotation(ExtensionContext context,
			Class<A> annotationType) {
		try {
			return (Optional<A>) FIND_CLOSEST_ENCLOSING_ANNOTATION.invoke(null, context, annotationType);
		}
		catch (ReflectiveOperationException ex) {
			throw internalError(ex);
		}
	}

	@SuppressWarnings("unchecked")
	public static <A extends Annotation> List<Annotation> findAnnotatedAnnotations(AnnotatedElement element,
			Class<A> annotation) {
		try {
			return (List<Annotation>) FIND_ANNOTATED_ANNOTATION.invoke(null, element, annotation);
		}
		catch (ReflectiveOperationException ex) {
			throw internalError(ex);
		}
	}

	public static boolean isContainerAnnotation(Annotation annotation) {
		try {
			return (boolean) IS_CONTAINER_ANNOTATION.invoke(null, annotation);
		}
		catch (ReflectiveOperationException ex) {
			throw internalError(ex);
		}
	}

}
