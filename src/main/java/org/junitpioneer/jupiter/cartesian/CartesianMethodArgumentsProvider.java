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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * If you are implementing an {@link org.junit.jupiter.params.provider.ArgumentsProvider ArgumentsProvider}
 * for {@link CartesianTest}, it has to implement this interface <b>as well</b> to provide arguments simultaneously
 * for all parameters in a test method. For more information, see
 * <a href="https://junit-pioneer.org/docs/cartesian-product/" target="_top">the Cartesian product documentation</a>.
 *
 * @see org.junit.jupiter.params.provider.ArgumentsProvider
 */
public interface CartesianMethodArgumentsProvider extends CartesianArgumentsProvider {

	/**
	 * Provides a {@link Sets} object, containing the arguments for each parameter in order,
	 * to be used for the {@code @CartesianTest}.
	 * For more information, see
	 * <a href="https://junit-pioneer.org/docs/cartesian-product/" target="_top">the Cartesian product documentation</a>.
	 *
	 * @param context the current extension context; never {@code null}
	 * @return a {@code Sets} object; never {@code null}
	 */
	Sets provideArguments(ExtensionContext context) throws Exception;

	/**
	 * Class for defining sets to a {@code CartesianTest} execution.
	 */
	class Sets {

		List<List<?>> sets = new ArrayList<>(); // NOSONAR

		/**
		 * Creates a single set of distinct objects (according to
		 * {@link Object#equals(Object)}) for a CartesianProductTest
		 * from the elements of the passed {@link java.util.Collection}.
		 *
		 * The passed argument does not have to be an instance of {@link java.util.Set}.
		 *
		 * @param items the objects we want to include in a single set
		 * @return the {@code Sets} object, for fluent set definitions
		 */
		public Sets add(Collection<?> items) {
			sets.add(new ArrayList<>(items));
			return this;
		}

		/**
		 * Creates a single set of distinct objects (according to
		 * {@link Object#equals(Object)}) for a CartesianProductTest
		 * from the passed objects.
		 *
		 * @param items the objects we want to include in a single set
		 * @return the {@code Sets} object, for fluent set definitions
		 */
		public Sets add(Object... items) {
			return add(Arrays.asList(items));
		}

		/**
		 * Creates a single set of distinct objects (according to
		 * {@link Object#equals(Object)}) for a CartesianProductTest
		 * from the elements of the passed {@link java.util.stream.Stream}.
		 *
		 * @param items the objects we want to include in a single set
		 * @return the {@code Sets} object, for fluent set definitions
		 */
		public Sets add(Stream<?> items) {
			return add(items.collect(Collectors.toList()));
		}

		List<List<?>> get() {
			return sets;
		}

	}

}
