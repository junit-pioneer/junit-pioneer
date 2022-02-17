package org.junitpioneer.jupiter.cartesian;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Class for defining sets to a {@code CartesianTest} execution with arguments for each parameter
 * in the order in which they appear in the test method.
 *
 * <p>Use the static factory methods
 * {@link ArgumentSets#create() create} or
 * {@link ArgumentSets#argumentsForFirstParameter(Object[]) argumentsForFirstParameter}
 * to create instances and call
 * {@link ArgumentSets#argumentsForNextParameter(Object[]) argumentsForNextParameter}
 * for each parameter after the first.
 * </p>
 */
public class ArgumentSets {

	private final List<List<?>> argumentSets;

	private ArgumentSets() {
		this.argumentSets = new ArrayList<>();
	}

	private ArgumentSets(List<?> arguments) {
		this();
		add(arguments);
	}

	private ArgumentSets add(List<?> arguments) {
		argumentSets.add(new ArrayList<>(arguments));
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
	 * <p>
	 * The passed argument does not have to be an instance of {@link java.util.Set Set}.
	 *
	 * @param arguments the objects that should be passed to the parameter
	 * @return a new {@link ArgumentSets} object
	 */
	public static <T> ArgumentSets argumentsForFirstParameter(Collection<T> arguments) {
		return new ArgumentSets(new ArrayList<>(arguments));
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
		return new ArgumentSets(arguments.collect(toList()));
	}

	/**
	 * Creates a single set of distinct objects (according to their
	 * {@link Object#equals(Object) equals}) for the first parameter of
	 * a {@code CartesianTest} from the elements of the passed
	 * {@link Collection Collection}.
	 * <p>
	 * The passed argument does not have to be an instance of {@link java.util.Set Set}.
	 *
	 * @param arguments the objects that should be passed to the parameter
	 * @return this {@link ArgumentSets} object, for fluent set definitions
	 */
	public final <T> ArgumentSets argumentsForNextParameter(Collection<T> arguments) {
		return add(new ArrayList<>(arguments));
	}

	/**
	 * Creates a single set of distinct objects (according to their
	 * {@link Object#equals(Object) equals}) for the first parameter of
	 * a {@code CartesianTest} from the elements of the passed
	 * objects.
	 *
	 * @param arguments the objects that should be passed to the parameter
	 * @return this {@link ArgumentSets} object, for fluent set definitions
	 */
	@SafeVarargs
	public final <T> ArgumentSets argumentsForNextParameter(T... arguments) {
		return add(Arrays.asList(arguments));
	}

	/**
	 * Creates a single set of distinct objects (according to their
	 * {@link Object#equals(Object) equals}) for the first parameter of
	 * a {@code CartesianTest} from the elements of the passed
	 * {@link Stream Stream}.
	 *
	 * @param arguments the objects that should be passed to the parameter
	 * @return this {@link ArgumentSets} object, for fluent set definitions
	 */
	public final <T> ArgumentSets argumentsForNextParameter(Stream<T> arguments) {
		return add(arguments.collect(toList()));
	}

	List<List<?>> getArguments() {
		return argumentSets;
	}

}
