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
import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junitpioneer.jupiter.PioneerException;

/**
 * Copy of {@code org.junitpioneer.jupiter.PioneerAnnotationUtils}.
 *
 * <p>This copy is necessary to keep all utils package-visible. In order not to duplicate
 * a lot of code, which adds the challenge to keep the implementations tested and in sync,
 * we use reflection to access the package-visible original implementation.</p>
 */
class PioneerAnnotationUtils {

	private static final Method FIND_CLOSEST_ENCLOSING_ANNOTATION;

	static {
		try {
			FIND_CLOSEST_ENCLOSING_ANNOTATION = Class
					.forName("org.junitpioneer.jupiter.PioneerAnnotationUtils")
					.getMethod("findClosestEnclosingAnnotation", ExtensionContext.class, Class.class);
			FIND_CLOSEST_ENCLOSING_ANNOTATION.setAccessible(true); // NOSONAR this is necessary to reach the method
		}
		catch (ReflectiveOperationException ex) {
			throw new PioneerException("Pioneer could not initialize itself.", ex);
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
			throw new PioneerException("Internal Pioneer error.", ex);
		}
	}

}
