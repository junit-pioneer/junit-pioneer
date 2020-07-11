/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.params;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Copy of {@code org.junitpioneer.jupiter.PioneerAnnotationUtils}.
 *
 * <p>This copy is necessary to keep all utils package-visible. In order not to duplicate
 * a lot of code, which adds the challenge to keep the implementations tested and in sync,
 * we use reflection to access the package-visible original implementation.</p>
 */
class PioneerAnnotationUtils {

	private static final Class<?> PIONEER_ANNOTATION_UTILS;
	private static final Method FIND_CLOSEST_ENCLOSING_ANNOTATION;

	static {
		try {
			PIONEER_ANNOTATION_UTILS = Class.forName("org.junitpioneer.jupiter.PioneerAnnotationUtils");
			FIND_CLOSEST_ENCLOSING_ANNOTATION = PIONEER_ANNOTATION_UTILS
					.getMethod("findClosestEnclosingAnnotation", ExtensionContext.class, Class.class);
			FIND_CLOSEST_ENCLOSING_ANNOTATION.setAccessible(true);
		}
		catch (ReflectiveOperationException ex) {
			throw new RuntimeException("Pioneer could not initialize itself.", ex);
		}
	}

	private PioneerAnnotationUtils() {
		// private constructor to prevent instantiation of utility class
	}

	@SuppressWarnings("unchecked")
	public static <A extends Annotation> Optional<A> findClosestEnclosingAnnotation(ExtensionContext context,
			Class<A> annotationType) {
		try {
			return (Optional<A>) FIND_CLOSEST_ENCLOSING_ANNOTATION.invoke(null, context, annotationType);
		}
		catch (ReflectiveOperationException ex) {
			throw new RuntimeException("Internal Pioneer error.", ex);
		}
	}

}
