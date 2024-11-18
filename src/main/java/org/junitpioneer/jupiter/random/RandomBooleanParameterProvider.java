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

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.List;

import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.AssertTrue;

public class RandomBooleanParameterProvider extends RandomParameterProvider {

	public RandomBooleanParameterProvider() {
		// recreate default constructor to prevent compiler warning
	}

	@Override
	public List<Class<?>> getSupportedParameterTypes() {
		return List.of(boolean.class, Boolean.class);
	}

	@Override
	public Object provideRandomParameter(Parameter parameter, Field field) {
		if (IS_JAKARTA_VALIDATION_PRESENT) {
			var mustBeTrue = findAnnotation(parameter, AssertTrue.class)
					.or(() -> findAnnotation(field, AssertTrue.class));
			var mustBeFalse = findAnnotation(parameter, AssertFalse.class)
					.or(() -> findAnnotation(field, AssertFalse.class));
			ensureAtMostOneConstraintIsActive(mustBeFalse, mustBeTrue);
			if (mustBeTrue.isPresent()) {
				return true;
			} else if (mustBeFalse.isPresent()) {
				return false;
			}
		}
		return random.nextBoolean();
	}

}
