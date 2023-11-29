/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.cartesian;

import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Class for defining sets to a {@code CartesianTest} execution with arguments for each parameter
 * in the order in which they appear in the test method.
 *
 * <p>Use the static factory method {@link ArgumentSets#argumentsForFirstParameter(Object[]) argumentsForFirstParameter}
 * to create an instance and call {@link ArgumentSets#argumentsForNextParameter(Object[]) argumentsForNextParameter}
 * for each parameter after the first. Alternatively, call the static factory method
 * {@link ArgumentSets#create() create} to create an instance call {@code argumentsForNextParameter} for each parameter.</p>
 */
public class ArgumentSets {

	private final List<List<?>> arguments;

	private ArgumentSets() {
		this.arguments = new ArrayList<>();
	}

	private ArgumentSets(Collection<?> arguments) {
		this();
		add(arguments);
	}

	private ArgumentSets add(Collection<?> arguments) {
		this.arguments.add(new ArrayList<>(arguments));
		return this;
	}

	/**
	 * Creates a new {@link ArgumentSets} without arguments for any parameters.
	 */
	public static ArgumentSets create() {
		return new ArgumentSets();
	}

	/**
	 * Creates a single set of distinct objects (according to their
	 * {@link Object#equals(Object) equals}) for the first parameter of
	 * a {@code CartesianTest} from the elements of the passed
	 * {@link java.util.Collection Collection}.
	 *
	 * <p>The passed argument does not have to be an instance of {@link java.util.Set Set}.</p>
	 *
	 * @param arguments the objects that should be passed to the parameter
	 * @return a new {@link ArgumentSets} object
	 */
	public static <T> ArgumentSets argumentsForFirstParameter(Collection<T> arguments) {
		return new ArgumentSets(arguments);
	}

	/**
	 * Creates a single set of distinct objects (according to their
	 * {@link Object#equals(Object) equals}) for the first parameter of
	 * a {@code CartesianTest} from the elements of the passed
	 * objects.
	 *
	 * @param arguments the objects that should be passed to the parameter
	 * @return a new {@link ArgumentSets} object
	 */
	@SafeVarargs
	// passing varargs on to another varargs method causes a warning
	// that can't be fixed; only suppressed
	@SuppressWarnings("varargs")
	public static <T> ArgumentSets argumentsForFirstParameter(T... arguments) {
		return new ArgumentSets(Arrays.asList(arguments));
	}

	/**
	 * Creates a single set of distinct objects (according to their
	 * {@link Object#equals(Object) equals}) for the first parameter of
	 * a {@code CartesianTest} from the elements of the passed
	 * {@link java.util.stream.Stream Stream}.
	 *
	 * @param arguments the objects that should be passed to the parameter
	 * @return a new {@link ArgumentSets} object
	 */
	public static <T> ArgumentSets argumentsForFirstParameter(Stream<T> arguments) {
		return new ArgumentSets(arguments.collect(toUnmodifiableList()));
	}

	/**
	 * Creates a single set of distinct objects (according to their
	 * {@link Object#equals(Object) equals}) for the next parameter of
	 * a {@code CartesianTest} from the elements of the passed
	 * {@link Collection Collection}.
	 *
	 * <p>The passed argument does not have to be an instance of {@link java.util.Set Set}.</p>
	 *
	 * @param arguments the objects that should be passed to the parameter
	 * @return this {@link ArgumentSets} object, for fluent set definitions
	 */
	public final <T> ArgumentSets argumentsForNextParameter(Collection<T> arguments) {
		return add(arguments);
	}

	/**
	 * Creates a single set of distinct objects (according to their
	 * {@link Object#equals(Object) equals}) for the next parameter of
	 * a {@code CartesianTest} from the elements of the passed
	 * objects.
	 *
	 * @param arguments the objects that should be passed to the parameter
	 * @return this {@link ArgumentSets} object, for fluent set definitions
	 */
	@SafeVarargs
	// passing varargs on to another varargs method causes a warning
	// that can't be fixed; only suppressed
	@SuppressWarnings("varargs")
	public final <T> ArgumentSets argumentsForNextParameter(T... arguments) {
		return add(Arrays.asList(arguments));
	}

	/**
	 * Creates a single set of distinct objects (according to their
	 * {@link Object#equals(Object) equals}) for the next parameter of
	 * a {@code CartesianTest} from the elements of the passed
	 * {@link Stream Stream}.
	 *
	 * @param arguments the objects that should be passed to the parameter
	 * @return this {@link ArgumentSets} object, for fluent set definitions
	 */
	public final <T> ArgumentSets argumentsForNextParameter(Stream<T> arguments) {
		return add(arguments.collect(toUnmodifiableList()));
	}

	List<List<?>> getArguments() {
		return arguments;
	}

}
