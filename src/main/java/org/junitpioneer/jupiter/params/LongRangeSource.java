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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.params.provider.ArgumentsSource;

/**
 * {@code @LongRangeSource} is an {@link ArgumentsSource} that provides access to a range of {@code long} values.
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
@RangeClass(LongRange.class)
public @interface LongRangeSource {

	/**
	 * The starting point of the range, inclusive.
	 */
	long from();

	/**
	 * The end point of the range, exclusive.
	 */
	long to();

	/**
	 * The size of the step between the {@code from} and the {@code to}.
	 */
	long step() default 1;

	/**
	 * Whether the range is closed (inclusive of the {@link #to()}) or not.
	 */
	boolean closed() default false;

}
