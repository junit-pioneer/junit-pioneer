/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.params;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
 * @see ShortRangeSource
 * @see LongRangeSource
 * @see ByteRangeSource
 * @see DoubleRangeSource
 * @see FloatRangeSource
 */
class RangeSourceProvider implements ArgumentsProvider {

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		// @formatter:off
		// since it's a method annotation, the element will always be present
		List<Annotation> argumentsSources = context
				.getElement()
				.map(method -> Arrays
						.stream(method.getAnnotations())
						.filter(annotations -> Arrays
								.stream(annotations.annotationType().getAnnotationsByType(ArgumentsSource.class))
								.anyMatch(annotation -> getClass().equals(annotation.value())))
						.collect(Collectors.toList()))
				.get();

		Preconditions.condition(
				argumentsSources.size() == 1,
				() -> String.format(
						"Expected exactly one annotation to provide an ArgumentSource, found %d.",
						argumentsSources.size()));
		// @formatter:on

		Annotation argumentsSource = argumentsSources.get(0);
		Class<? extends Annotation> argumentsSourceClass = argumentsSource.annotationType();
		Class<? extends Range> rangeClass = argumentsSourceClass.getAnnotation(RangeClass.class).value();

		Range<?> range = (Range) rangeClass.getConstructors()[0].newInstance(argumentsSource);
		range.validate();
		return asStream(range).map(Arguments::of);
	}

	private Stream<?> asStream(Range r) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(r, Spliterator.ORDERED), false);
	}

}
