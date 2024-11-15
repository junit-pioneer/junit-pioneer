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

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Arrays;

/**
 * Utility class for finding the corresponding field to a constructor parameter.
 */
public class FieldFinder {

	public static Field getMatchingField(Class<?> clazz, Parameter parameter, int constructorIndex) {
		String paramName = parameter.getName();
		Class<?> paramType = parameter.getType();

		Field field;
		field = findFieldByName(clazz, paramName);
		if (field != null) {
			return field;
		}

		field = findFieldByType(clazz, paramType);
		if (field != null) {
			return field;
		}

		field = findFieldByIndex(clazz, paramType, constructorIndex);
		if (field != null) {
			return field;
		}

		return null;
	}

	private static Field findFieldByIndex(Class<?> clazz, Class<?> paramType, int constructorIndex) {
		var fields = clazz.getDeclaredFields();
		if (constructorIndex < fields.length) {
			Field candidate = fields[constructorIndex];
			if (candidate.getType().equals(paramType)) {
				return candidate;
			}
		}
		return null;
	}

	private static Field findFieldByType(Class<?> clazz, Class<?> paramType) {
		if (Arrays.stream(clazz.getDeclaredFields()).filter(field -> field.getType().equals(paramType)).count() == 1) {
			for (Field field : clazz.getDeclaredFields()) {
				if (field.getType().equals(paramType)) {
					return field;
				}
			}
		}
		return null;
	}

	private static Field findFieldByName(Class<?> clazz, String paramName) {
		try {
			return clazz.getDeclaredField(paramName);
		}
		catch (NoSuchFieldException e) {
			return null;
		}
	}

}
