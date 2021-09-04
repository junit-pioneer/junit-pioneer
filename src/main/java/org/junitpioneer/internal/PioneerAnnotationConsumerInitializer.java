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
import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;

/**
 * This is a slight copy of the Jupiter's {@link org.junit.jupiter.params.support.AnnotationConsumerInitializer AnnotationConsumerInitializer}
 * except that it does not lookup the consumed annotation, but it used the
 * {@code AnnotationConsumerInitializer} is an internal helper class for
 * initializing {@link AnnotationConsumer AnnotationConsumers}.
 *
 */
public final class PioneerAnnotationConsumerInitializer {

	// @formatter:off
	private static final Predicate<Method> isAnnotationConsumerAcceptMethod = method ->
			method.getName().equals("accept")
			&& method.getParameterCount() == 1
			&& method.getParameterTypes()[0].isAnnotation();
	// @formatter:on

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T initialize(Annotation annotation, T instance) {
		if (instance instanceof AnnotationConsumer) {
			Method method = ReflectionSupport
					.findMethods(instance.getClass(), isAnnotationConsumerAcceptMethod,
						HierarchyTraversalMode.BOTTOM_UP)
					.get(0);
			Class<? extends Annotation> annotationType = (Class<? extends Annotation>) method.getParameterTypes()[0];
			if (annotationType.isInstance(annotation)) {
				initializeAnnotationConsumer((AnnotationConsumer) instance, annotation);
			} else {
				throw new JUnitException(instance.getClass().getName() + " must be used with an annotation of type "
						+ annotationType.getName());
			}
		}
		return instance;
	}

	private static <A extends Annotation> void initializeAnnotationConsumer(AnnotationConsumer<A> instance,
			A annotation) {
		try {
			instance.accept(annotation);
		}
		catch (Exception ex) {
			throw new JUnitException("Failed to initialize AnnotationConsumer: " + instance, ex);
		}
	}

}
