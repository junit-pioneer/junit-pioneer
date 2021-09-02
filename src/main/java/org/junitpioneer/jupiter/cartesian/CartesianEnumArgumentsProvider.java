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
 * except it does NOT support {@code @ParameterizedTest} and consumes a {@code Parameter}
 * instead of an annotation.
 */
class CartesianEnumArgumentsProvider implements CartesianArgumentsProvider {

	private CartesianTest.Enum enumSource;
	private Class<?> parameterType;

	@Override
	public void accept(Parameter parameter) {
		this.parameterType = parameter.getType();
		if (!Enum.class.isAssignableFrom(this.parameterType))
			throw new PreconditionViolationException(String
					.format(
						"Parameter of type %s must reference an Enum type (alternatively, use the annotation's 'value' attribute to specify the type explicitly)",
						this.parameterType));
		this.enumSource = AnnotationSupport
				.findAnnotation(parameter, CartesianTest.Enum.class)
				.orElseThrow(() -> new PreconditionViolationException(
					"Parameter has to be annotated with " + CartesianTest.Enum.class.getName()));
	}

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		Set<? extends Enum<?>> constants = getEnumConstants();
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

	private <E extends Enum<E>> Set<? extends E> getEnumConstants() {
		Class<E> enumClass = determineEnumClass();
		return EnumSet.allOf(enumClass);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <E extends Enum<E>> Class<E> determineEnumClass() {
		Class enumClass = enumSource.value();
		if (enumClass.equals(NullEnum.class)) {
			enumClass = this.parameterType;
		}
		return enumClass;
	}

	enum NullEnum {

	}

}
