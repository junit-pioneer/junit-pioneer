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

import static java.lang.String.format;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.List;

import org.junit.jupiter.api.extension.ParameterResolutionException;

public class RandomEnumParameterProvider extends RandomParameterProvider {

	public RandomEnumParameterProvider() {
		// recreate default constructor to prevent compiler warning
	}

	@Override
	public List<Class<?>> getSupportedParameterTypes() {
		return List.of(Enum.class);
	}

	@Override
	public Object provideRandomParameter(Parameter parameter, Field field) {
		Object[] constants = parameter.getType().getEnumConstants();
		if (constants == null)
			throw new ParameterResolutionException(format("Expected %s to be an enum type", parameter.getType()));
		return constants[random.nextInt(constants.length)];
	}

}
