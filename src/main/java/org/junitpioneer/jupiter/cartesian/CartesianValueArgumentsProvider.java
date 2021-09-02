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

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Array;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.support.AnnotationSupport;

/**
 * This is a slightly modified copy of Jupiter's {@code ValueSourceArgumentsProvider},
 * except it does NOT support {@code @ParameterizedTest} and can consume a {@code Parameter}
 * instead of an annotation.
 */
class CartesianValueArgumentsProvider implements CartesianArgumentsProvider {

	private Object[] arguments;

	private void getArgumentsFromAnnotation(CartesianTest.Values source) {
		// @formatter:off
		List<Object> arrays =
				// Declaration of <Object> is necessary due to a bug in Eclipse Photon.
				Stream.<Object> of(
						source.shorts(),
						source.bytes(),
						source.ints(),
						source.longs(),
						source.floats(),
						source.doubles(),
						source.chars(),
						source.booleans(),
						source.strings(),
						source.classes()
				)
				.filter(array -> Array.getLength(array) > 0)
				.collect(toList());
		// @formatter:on

		if (arrays.size() != 1)
			throw new PreconditionViolationException("Exactly one type of input must be provided in the @"
					+ CartesianTest.Values.class.getSimpleName() + " annotation, but there were " + arrays.size());

		Object originalArray = arrays.get(0);
		arguments = IntStream
				.range(0, Array.getLength(originalArray)) //
				.mapToObj(index -> Array.get(originalArray, index)) //
				.toArray();
	}

	@Override
	public void accept(Parameter parameter) {
		CartesianTest.Values source = AnnotationSupport
				.findAnnotation(parameter, CartesianTest.Values.class)
				.orElseThrow(() -> new PreconditionViolationException(
					"Parameter has to be annotated with " + CartesianTest.Values.class.getName()));
		getArgumentsFromAnnotation(source);
	}

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		return Stream.of(Arguments.of(arguments));
	}

}
