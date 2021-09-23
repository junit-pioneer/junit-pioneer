/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.cartesian;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

import java.lang.reflect.Parameter;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.support.AnnotationSupport;

/**
 * This is basically an enhanced copy of Jupiter's {@code EnumArgumentsProvider},
 * except it does NOT support {@code @ParameterizedTest} and implements {@link CartesianArgumentsProvider}
 * for use with {@code @CartesianTest}.
 *
 * @implNote This class does not implement {@code ArgumentsProvider} since the Jupiter's {@code EnumSource}
 * should be used for that.
 */
class CartesianEnumArgumentsProvider implements CartesianArgumentsProvider {

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context, Parameter parameter) {
		Class<?> parameterType = parameter.getType();
		if (!Enum.class.isAssignableFrom(parameterType))
			throw new PreconditionViolationException(String
					.format(
						"Parameter of type %s must reference an Enum type (alternatively, use the annotation's 'value' attribute to specify the type explicitly)",
						parameterType));
		CartesianTest.Enum enumSource = AnnotationSupport
				.findAnnotation(parameter, CartesianTest.Enum.class)
				.orElseThrow(() -> new PreconditionViolationException(
					"Parameter has to be annotated with " + CartesianTest.Enum.class.getName()));

		Set<? extends Enum<?>> constants = getEnumConstants(enumSource, parameterType);
		CartesianTest.Enum.Mode mode = enumSource.mode();
		String[] declaredConstantNames = enumSource.names();
		if (declaredConstantNames.length > 0) {
			Set<String> uniqueNames = stream(declaredConstantNames).collect(toSet());
			if (uniqueNames.size() != declaredConstantNames.length)
				throw new PreconditionViolationException("Duplicate enum constant name(s) found in " + enumSource);

			mode.validate(enumSource, constants, uniqueNames);
			constants.removeIf(constant -> !mode.select(constant, uniqueNames));
		}
		return constants.stream().map(Arguments::of);
	}

	private <E extends Enum<E>> Set<? extends E> getEnumConstants(CartesianTest.Enum enumSource,
			Class<?> parameterType) {
		Class<E> enumClass = determineEnumClass(enumSource, parameterType);
		return EnumSet.allOf(enumClass);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <E extends Enum<E>> Class<E> determineEnumClass(CartesianTest.Enum enumSource, Class<?> parameterType) {
		Class enumClass = enumSource.value();
		if (enumClass.equals(NullEnum.class)) {
			enumClass = parameterType;
		}
		return enumClass;
	}

	enum NullEnum {

	}

}
