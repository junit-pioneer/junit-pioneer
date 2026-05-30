/*
 * Copyright 2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.random;

import static java.util.stream.Collectors.joining;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.platform.commons.support.AnnotationSupport;
import org.junitpioneer.internal.PioneerRandomUtils;

import jakarta.validation.constraints.Size;

public class RandomStringParameterProvider extends RandomParameterProvider {

	public RandomStringParameterProvider() {
		// recreate default constructor to prevent compiler warning
	}

	@Override
	public List<Class<?>> getSupportedParameterTypes() {
		return List.of(String.class, CharSequence.class);
	}

	@Override
	public Object provideRandomParameter(Parameter parameter, Field field) {
		int min = 3;
		int max = 10;
		if (IS_JAKARTA_VALIDATION_PRESENT) {
			var sizeConstraint = AnnotationSupport
					.findAnnotation(parameter, Size.class)
					.or(() -> AnnotationSupport.findAnnotation(field, Size.class));
			if (sizeConstraint.isPresent()) {
				var size = sizeConstraint.get();
				min = size.min();
				max = size.max() + 1; // Size.max is inclusive, but boundedNextInt is exclusive
			}
		}
		int length = PioneerRandomUtils.boundedNextInt(random, min, max);
		return IntStream
				.range(0, length)
				.mapToObj(ignored -> PioneerRandomUtils.randomAlphanumericCharacter(random))
				.collect(joining());
	}

}
