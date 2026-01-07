/*
 * Copyright 2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.internal;

import static java.lang.String.format;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.extension.ParameterResolutionException;

/**
 * Utility class for finding the corresponding field to a constructor parameter.
 */
public final class FieldFinder {

	private FieldFinder() {
	}

	public static Field getMatchingField(Class<?> clazz, Parameter parameter, int constructorIndex) {
		String paramName = parameter.getName();
		Class<?> paramType = parameter.getType();

		return findFieldByName(clazz, paramName)
				.or(() -> findFieldByType(clazz, paramType))
				.or(() -> findFieldByIndex(clazz, paramType, constructorIndex))
				.orElseThrow(() -> new ParameterResolutionException(
					format("Could not find matching field for constructor parameter %s when trying to instantiate %s",
						parameter, clazz)));
	}

	private static Optional<Field> findFieldByIndex(Class<?> clazz, Class<?> paramType, int constructorIndex) {
		var fields = clazz.getDeclaredFields();
		if (constructorIndex < fields.length) {
			Field candidate = fields[constructorIndex];
			if (candidate.getType().equals(paramType)) {
				return Optional.of(candidate);
			}
		}
		return Optional.empty();
	}

	private static Optional<Field> findFieldByType(Class<?> clazz, Class<?> paramType) {
		if (Arrays.stream(clazz.getDeclaredFields()).filter(field -> field.getType().equals(paramType)).count() == 1) {
			for (Field field : clazz.getDeclaredFields()) {
				if (field.getType().equals(paramType)) {
					return Optional.of(field);
				}
			}
		}
		return Optional.empty();
	}

	private static Optional<Field> findFieldByName(Class<?> clazz, String paramName) {
		try {
			return Optional.of(clazz.getDeclaredField(paramName));
		}
		catch (NoSuchFieldException e) {
			return Optional.empty();
		}
	}

}
