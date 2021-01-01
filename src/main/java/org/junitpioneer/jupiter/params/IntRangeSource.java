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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.params.provider.ArgumentsSource;

/**
 * {@code @IntRangeSource} is an {@link ArgumentsSource} that provides access to a range of {@code int} values.
 *
 * <p>The supplied values will be provided as arguments to the annotated {@code @ParameterizedTest} method.</p>
 *
 * <p>For more details and examples, see
 * <a href="https://junit-pioneer.org/docs/range-sources/" target="_top">the documentation on <code>Range Sources</code></a>
 * </p>
 *
 * @since 0.5
 * @see ArgumentsSource
 * @see org.junit.jupiter.params.ParameterizedTest
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ArgumentsSource(RangeSourceArgumentsProvider.class)
@RangeClass(IntRange.class)
public @interface IntRangeSource {

	/**
	 * The starting point of the range, inclusive.
	 */
	int from();

	/**
	 * The end point of the range, exclusive.
	 */
	int to();

	/**
	 * The size of the step between the {@code from} and the {@code to}.
	 */
	int step() default 1;

	/**
	 * Whether the range is closed (inclusive of the {@link #to()}) or not.
	 */
	boolean closed() default false;

}
