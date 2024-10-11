/*
 * Copyright 2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junitpioneer.internal.PioneerRandomUtils;

class RandomParameterExtension implements ParameterResolver {

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return parameterContext.findAnnotation(org.junitpioneer.jupiter.Random.class).isPresent();
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		long seed = parameterContext.findAnnotation(org.junitpioneer.jupiter.Random.class).get().seed();
		return instantiate(parameterContext.getParameter().getType(), seed);
	}

	private static Object instantiate(Class<?> type, long seed) {
		Random random = new Random(seed);
		return instantiateComplexType(type, random);
	}

	private static Object instantiateComplexType(Class<?> type, Random random) {
		try {
			if (isSimpleParameterType(type)) {
				return createRandomParameter(type, random);
			}
			var allArgsConstructor = findAllArgsConstructor(type);
			var noArgsConstructor = findNoArgsConstructor(type);
			if (allArgsConstructor.isPresent()) {
				// instantiate all fields
				var args = Arrays
						.stream(allArgsConstructor.get().getParameters())
						.map(parameter -> createRandomParameter(parameter.getType(), random))
						.toArray();
				return allArgsConstructor.get().newInstance(args);
			}
			if (noArgsConstructor.isPresent()) {

			}
			throw new ExtensionConfigurationException(
				format("No suitable constructor was found for instantiating %s through @Random", type));
		}
		catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	private static boolean isSimpleParameterType(Class<?> type) {
		return PioneerRandomUtils.isSupportedType(type);
	}

	private static Object createRandomParameter(Class<?> parameterType, Random random) {
		return PioneerRandomUtils.randomObject(parameterType, random);
	}

	private static Optional<Constructor<?>> findAllArgsConstructor(Class<?> type) {
		var fields = Arrays.stream(type.getDeclaredFields()).map(Field::getType).collect(toList());
		return Arrays
				.stream(type.getDeclaredConstructors())
				.filter(constructor -> Arrays
						.stream(constructor.getParameters())
						.allMatch(parameter -> fields.contains(parameter.getType())))
				.findFirst();
	}

	private static Optional<Constructor<?>> findNoArgsConstructor(Class<?> type) {
		try {
			return Optional.of(type.getDeclaredConstructor());
		}
		catch (NoSuchMethodException e) {
			return Optional.empty();
		}
	}

}
