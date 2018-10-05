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

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.util.Preconditions;

/**
 * Provides a range of {@code int}s, as defined by {@link RangeSource}.
 *
 * @see RangeSource
 */
public class RangeSourceProvider implements ArgumentsProvider, AnnotationConsumer<RangeSource> {

	/**
	 * The range of values.
	 */
	private final List<Integer> values = new LinkedList<>();

	/**
	 * Whether the range is closed or not
	 */
	private boolean closed;

	@Override
	public void accept(RangeSource rangeSource) {
		Preconditions.condition(rangeSource.step() != 0,
			() -> String.format("Illegal %s. The step cannot be 0.", RangeSource.class.getSimpleName()));

		Preconditions.condition(rangeSource.from() != rangeSource.to(),
			() -> String.format("Illegal %s. Equal from and to will produce an empty range.",
				RangeSource.class.getSimpleName()));

		Preconditions.condition(
			rangeSource.from() < rangeSource.to() && rangeSource.step() > 0
					|| rangeSource.from() > rangeSource.to() && rangeSource.step() < 0,
			() -> String.format("Illegal %s. There's no way to get from %d to %d with a step of %d.",
				RangeSource.class.getSimpleName(), rangeSource.from(), rangeSource.to(), rangeSource.step()));

		closed = rangeSource.closed();

		// Once (if) Java 8 support is dropped, this boiler-plate can be cleaned up and we can just use
		// IntStream.iterate(rangeSource.from(), i -> i < rangeSource.to(), i -> i += rangeSource.step());
		int val = rangeSource.from();
		int to = rangeSource.to();
		int step = rangeSource.step();
		while (val < to && step > 0 || val > to && step < 0 || closed && val == to) {
			values.add(val);
			val += rangeSource.step();
		}
	}

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		return values.stream().map(Arguments::of);
	}
}
