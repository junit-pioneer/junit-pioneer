/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.params;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junitpioneer.internal.PioneerAnnotationUtils;
import org.junitpioneer.jupiter.CartesianAnnotationConsumer;

/**
 * Provides a range of {@link Number}s, as defined by an annotation which is its {@link ArgumentsSource}.
 * Such an annotation should have the following properties:
 * <ul>
 *     <li>{@code from} a primitive value for the "start" of the range.</li>
 *     <li>{@code to} a primitive value for the "end" of the range. {@code to} must have the same type as {@code from}.</li>
 *     <li>{@code step} a primitive value for the difference between each two values of the range.</li>
 *     <li>{@code closed} a {@code boolean} value describing if the range includes the last value (closed), or not (open).</li>
 * </ul>
 *
 * @see IntRangeSource
 * @see ShortRangeSource
 * @see LongRangeSource
 * @see ByteRangeSource
 * @see DoubleRangeSource
 * @see FloatRangeSource
 */
class RangeSourceArgumentsProvider implements ArgumentsProvider, CartesianAnnotationConsumer<Annotation> {

	private Annotation argumentsSource;

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		// argumentSource is present if fed through the CartesianAnnotationConsumer interface
		if (argumentsSource == null)
			initArgumentsSource(context);
		Class<? extends Annotation> argumentsSourceClass = argumentsSource.annotationType();
		Class<? extends Range> rangeClass = argumentsSourceClass.getAnnotation(RangeClass.class).value();

		Range<?> range = (Range<?>) rangeClass.getConstructors()[0].newInstance(argumentsSource);
		range.validate();
		return asStream(range).map(Arguments::of);
	}

	private void initArgumentsSource(ExtensionContext context) {
		// since it's a method annotation, the element will always be present
		AnnotatedElement element = context.getRequiredTestMethod();

		verifyNoContainerAnnotationIsPresent(element);
		List<Annotation> argumentsSources = PioneerAnnotationUtils
				.findAnnotatedAnnotations(element, ArgumentsSource.class);

		if (argumentsSources.size() != 1) {
			String message = String
					.format("Expected exactly one annotation to provide an ArgumentSource, found %d.",
						argumentsSources.size());
			throw new IllegalArgumentException(message);
		}

		argumentsSource = argumentsSources.get(0);
	}

	private void verifyNoContainerAnnotationIsPresent(AnnotatedElement element) {
		if (Stream.of(element.getAnnotations()).anyMatch(PioneerAnnotationUtils::isContainerAnnotation))
			throw new IllegalArgumentException(
				"Range source annotation should not be repeated for @ParameterizedTest. @ParameterizedTest should have exactly one argument source.");
	}

	private Stream<?> asStream(Range<?> r) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(r, Spliterator.ORDERED), false);
	}

	@Override
	public void accept(Annotation argumentsSource) {
		this.argumentsSource = argumentsSource;
	}

}
