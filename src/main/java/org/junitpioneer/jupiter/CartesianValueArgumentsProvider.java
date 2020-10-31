/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.platform.commons.PreconditionViolationException;

/**
 * This is basically a copy of ValueSourceArgumentsProvider,
 * except it does NOT support {@code @ParameterizedTest}.
 */
class CartesianValueArgumentsProvider implements Consumer<CartesianValueSource> {

	private Object[] arguments;

	@Override
	public void accept(CartesianValueSource source) {
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
					+ CartesianValueSource.class.getSimpleName() + " annotation, but there were " + arrays.size());

		Object originalArray = arrays.get(0);
		arguments = IntStream
				.range(0, Array.getLength(originalArray)) //
				.mapToObj(index -> Array.get(originalArray, index)) //
				.toArray();
	}

	public Stream<Object> provideArguments() {
		return Arrays.stream(arguments);
	}

}
