/*
 * Copyright 2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.random;

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
import static org.junit.platform.commons.support.AnnotationSupport.findRepeatableAnnotations;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.function.Predicate;

import org.junit.jupiter.api.extension.ParameterResolutionException;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public abstract class RandomNumberProvider<T extends Number> extends RandomParameterProvider {

	private final Long defaultMinValue;
	private final Long defaultMaxValue;

	public RandomNumberProvider(Long defaultMinValue, Long defaultMaxValue) {
		this.defaultMinValue = defaultMinValue;
		this.defaultMaxValue = defaultMaxValue;
	}

	@Override
	public Object provideRandomParameter(Parameter parameter, Field field) {
		if (IS_JAKARTA_VALIDATION_PRESENT) {
			var min = findRepeatableAnnotations(parameter, Min.class)
					.stream()
					.map(Min::value)
					.filter(withinRange())
					.reduce(Math::min)
					.or(() -> findRepeatableAnnotations(field, Min.class)
							.stream()
							.map(Min::value)
							.filter(withinRange())
							.reduce(Math::min))
					.orElse(defaultMinValue);
			var max = findRepeatableAnnotations(parameter, Max.class)
					.stream()
					.map(Max::value)
					.filter(withinRange())
					.reduce(Math::max)
					.or(() -> findRepeatableAnnotations(field, Max.class)
							.stream()
							.map(Max::value)
							.filter(withinRange())
							.reduce(Math::max))
					.orElse(defaultMaxValue);
			var positive = findAnnotation(parameter, Positive.class).or(() -> findAnnotation(field, Positive.class));
			var positiveOrZero = findAnnotation(parameter, PositiveOrZero.class)
					.or(() -> findAnnotation(field, PositiveOrZero.class));
			var negative = findAnnotation(parameter, Negative.class).or(() -> findAnnotation(field, Negative.class));
			var negativeOrZero = findAnnotation(parameter, NegativeOrZero.class)
					.or(() -> findAnnotation(field, NegativeOrZero.class));
			ensureAtMostOneConstraintIsActive(positive, positiveOrZero, negative, negativeOrZero);
			if (max <= min) {
				throw new ParameterResolutionException(
					"Invalid range between @Max and @Min. Note that @Min is inclusive and @Max is exclusive.");
			}

			if (positive.isPresent() && min <= 0) {
				min = 1L;
			}
			if (positiveOrZero.isPresent() && min < 0) {
				min = 0L;
			}
			if (negative.isPresent() && max > 0) {
				max = 0L;
			}
			if (negativeOrZero.isPresent() && max >= 0) {
				max = 0L;
			}

			return provideRandomNumber(min, max);
		}
		return getDefaultRandomNumber();
	}

	private Predicate<? super Long> withinRange() {
		return number -> number >= defaultMinValue && number < defaultMaxValue;
	}

	public abstract T getDefaultRandomNumber();

	public abstract T provideRandomNumber(Long min, Long max);

}
