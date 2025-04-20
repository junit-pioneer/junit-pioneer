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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.ServiceLoader;

import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junitpioneer.internal.FieldFinder;
import org.junitpioneer.jupiter.random.RandomParameterProvider;

class RandomParameterExtension implements ParameterResolver {

	private List<RandomParameterProvider> providers;

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return parameterContext.findAnnotation(org.junitpioneer.jupiter.Random.class).isPresent();
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		this.providers = ServiceLoader
				.load(RandomParameterProvider.class)
				.stream()
				.map(ServiceLoader.Provider::get)
				.collect(toList());
		var randomAnnotation = parameterContext.findAnnotation(org.junitpioneer.jupiter.Random.class);
		if (randomAnnotation.isEmpty()) {
			throw new ExtensionConfigurationException("No @Random found on parameter");
		}
		var random = new Random(randomAnnotation.get().seed()); //NOSONAR should only be used for testing
		this.providers.forEach(provider -> provider.init(random));
		return instantiate(parameterContext.getParameter());
	}

	private Object instantiate(Parameter parameter) {
		return instantiateComplexType(parameter, null);
	}

	private Object instantiateComplexType(Parameter parameter, Field correspondingField) {
		try {
			if (isSupportedParameterType(parameter.getType())) {
				return createRandomParameter(parameter, correspondingField);
			}
			var allArgsConstructor = findAllArgsConstructor(parameter.getType());
			var noArgsConstructor = findNoArgsConstructor(parameter.getType());
			if (allArgsConstructor.isPresent()) {
				// instantiate all fields
				var parameters = allArgsConstructor.get().getParameters();
				var args = new Object[parameters.length];
				for (int i = 0; i < parameters.length; i++) {
					Field field = FieldFinder.getMatchingField(parameter.getType(), parameters[i], i);
					args[i] = instantiateComplexType(parameters[i], field);
				}
				return allArgsConstructor.get().newInstance(args);
			}
			if (noArgsConstructor.isPresent()) {
				var result = noArgsConstructor.get().newInstance();
				Field[] fields = parameter.getType().getDeclaredFields();
				for (Field field : fields) {
					var setter = hasSetterMethod(field, parameter.getType());
					var setterParam = setter.getParameters()[0];
					var arg = instantiateComplexType(setterParam, field);
					setter.invoke(result, arg);
				}
				return result;
			}
			throw new ExtensionConfigurationException(format(
				"No suitable constructor was found for instantiating %s through @Random (could be a missing random parameter provider).",
				parameter.getType().getSimpleName()));
		}
		catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new ParameterResolutionException("Unexpected error while creating random parameter.", e);
		}
	}

	private Method hasSetterMethod(Field field, Class<?> type) {
		String name = normalize(field.getName());
		Class<?> fieldType = field.getType();
		try {
			return type.getMethod("set" + name, fieldType);
		}
		catch (Exception exception) {
			throw new ParameterResolutionException(
				format("Could not instantiate %s because setter was not found for %s", type.getSimpleName(),
					field.getName()),
				exception);
		}
	}

	private String normalize(String name) {
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}

	private boolean isSupportedParameterType(Class<?> type) {
		return providers.stream().anyMatch(provider -> supportsParameterType(provider, type));
	}

	private Object createRandomParameter(Parameter parameter, Field correspondingField) {
		return providers
				.stream()
				.filter(provider -> supportsParameterType(provider, parameter.getType()))
				.map(provider -> provider.provideRandomParameter(parameter, correspondingField))
				.findFirst()
				.orElseThrow(() -> new ParameterResolutionException(
					format("Unable to instantiate random parameter %s, there was a problem with the providers.",
						parameter.getType())));
	}

	private boolean supportsParameterType(RandomParameterProvider provider, Class<?> type) {
		return provider.getSupportedParameterTypes().stream().anyMatch(supported -> supported.isAssignableFrom(type));
	}

	private static Optional<Constructor<?>> findAllArgsConstructor(Class<?> type) {
		return Arrays.stream(type.getConstructors()).filter(RandomParameterExtension::isAllArgsConstructor).findFirst();
	}

	private static boolean isAllArgsConstructor(Constructor<?> constructor) {
		var fieldCount = Arrays
				.stream(constructor.getDeclaringClass().getDeclaredFields())
				.filter(field -> !Modifier.isStatic(field.getModifiers()))
				.count();
		return constructor.getParameters().length == fieldCount;
	}

	private static Optional<Constructor<?>> findNoArgsConstructor(Class<?> type) {
		try {
			return Optional.of(type.getConstructor());
		}
		catch (NoSuchMethodException e) {
			return Optional.empty();
		}
	}

}
