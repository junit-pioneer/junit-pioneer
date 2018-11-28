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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.platform.commons.util.Preconditions;

/**
 * Provides a range of {@link Number}s, as defined by an annotation which is its {@link ArgumentsSource}.
 * Such an annotation should have the following properties:
 * <ul>
 *     <li>{@code from} a primitive value for the "start" of the range.</li>
 *     <li>{@code to} a primitive value for the "end" of the range. {@code to} must have the same type as {@code from}.</li>
 *     <li>{@code step} a primitive value for the difference between each two values of the range.</li>
 *     <li>{@code closed} a {@code boolean} value describing if the range includes the last value (cloded), or not (open).</li>
 * </ul>
 *
 * @see IntRangeSource
 */
public class RangeSourceProvider implements ArgumentsProvider {

	/**
	 * {@link Function}s to convert {@link Number}s to primitive wrappers.
	 */
	private final List<Number> values = new LinkedList<>();

	/**
	 * A map from the (wrapper) class to produce, to a function that can produce it from a {@link Number}
	 */
	private static final Map<Class<?>, Function<Number, ?>> primitiveMappers = new HashMap<>();
	static {
		primitiveMappers.put(Byte.class, Number::byteValue);
		primitiveMappers.put(Short.class, Number::shortValue);
		primitiveMappers.put(Integer.class, Number::intValue);
		primitiveMappers.put(Long.class, Number::longValue);
		primitiveMappers.put(Float.class, Number::floatValue);
		primitiveMappers.put(Double.class, Number::doubleValue);
	}

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		// Since it's a method annotation, the element will always be present.
		List<Annotation> argumentsSources = context.getElement().map(e -> Arrays.stream(e.getAnnotations()).filter(
			as -> Arrays.stream(as.annotationType().getAnnotationsByType(ArgumentsSource.class)).anyMatch(
				a -> getClass().equals(a.value()))).collect(Collectors.toList())).get();

		Preconditions.condition(argumentsSources.size() == 1,
			() -> String.format("Expected exactly one annotation to provide an ArgumentSource, found %d.",
				argumentsSources.size()));

		Annotation argumentsSource = argumentsSources.get(0);
		Class<? extends Annotation> argumentsSourceClass = argumentsSource.annotationType();

		Method stepMethod = argumentsSourceClass.getMethod("step");
		Number stepValue = (Number) stepMethod.invoke(argumentsSource);
		double stepDouble = stepValue.doubleValue();
		Preconditions.condition(stepDouble != 0.0, "Illegal range. The step cannot be 0.");

		Method fromMethod = argumentsSourceClass.getMethod("from");
		Number fromValue = (Number) fromMethod.invoke(argumentsSource);
		double fromDouble = fromValue.doubleValue();

		Method toMethod = argumentsSourceClass.getMethod("to");
		Number toValue = (Number) toMethod.invoke(argumentsSource);
		double toDouble = toValue.doubleValue();

		Preconditions.condition(fromDouble != toDouble,
			"Illegal range. Equal from and to will produce an empty range.");

		Preconditions.condition(fromDouble < toDouble && stepDouble > 0.0 || fromDouble > toDouble && stepDouble < 0.0,
			() -> String.format("Illegal range. There's no way to get from %f to %f with a step of %f.", fromDouble,
				toDouble, stepDouble));

		Method closeMethod = argumentsSourceClass.getMethod("closed");
		Boolean closedValue = (Boolean) closeMethod.invoke(argumentsSource);

		// Once (if) Java 8 support is dropped, this boiler-plate can be cleaned up and we can just use
		// IntStream.iterate(rangeSource.from(), i -> i < rangeSource.to(), i -> i += rangeSource.step());
		double val = fromDouble;
		while (val < toDouble && stepDouble > 0.0 || val > toDouble && stepDouble < 0.0
				|| closedValue && val == toDouble) {
			values.add(val);
			val += stepDouble;
		}

		return values.stream().map(primitiveMappers.get(fromValue.getClass())).map(Arguments::of);
	}
}
