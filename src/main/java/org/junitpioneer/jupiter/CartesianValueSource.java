/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.params.provider.ArgumentsSource;

/**
 * {@code @CartesianValueSource} is an argument source which provides access to
 * an array of literal values.
 *
 * <p>Supported types include {@link #shorts}, {@link #bytes}, {@link #ints},
 * {@link #longs}, {@link #floats}, {@link #doubles}, {@link #chars},
 * {@link #booleans}, {@link #strings}, and {@link #classes}. Note, however,
 * that only one of the supported types may be specified per
 * {@code @CartesianValueSource} declaration.
 *
 * <p>The supplied literal values will be provided as an argument source to
 * the corresponding parameter of the annotated {@code @CartesianProductTest} method.
 *
 * <p>This annotation is {@link Repeatable}. You should declare one
 * {@code @CartesianValueSource} per parameter.
 * </p>
 *
 * @see CartesianProductTest
 *
 * @since 1.0
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(CartesianValueSource.CartesianValueSources.class)
@ArgumentsSource(CartesianValueArgumentsProvider.class)
public @interface CartesianValueSource {

	/**
	 * The {@code short} values to use as sources of arguments; must not be empty.
	 */
	short[] shorts() default {};

	/**
	 * The {@code byte} values to use as sources of arguments; must not be empty.
	 */
	byte[] bytes() default {};

	/**
	 * The {@code int} values to use as sources of arguments; must not be empty.
	 */
	int[] ints() default {};

	/**
	 * The {@code long} values to use as sources of arguments; must not be empty.
	 */
	long[] longs() default {};

	/**
	 * The {@code float} values to use as sources of arguments; must not be empty.
	 */
	float[] floats() default {};

	/**
	 * The {@code double} values to use as sources of arguments; must not be empty.
	 */
	double[] doubles() default {};

	/**
	 * The {@code char} values to use as sources of arguments; must not be empty.
	 */
	char[] chars() default {};

	/**
	 * The {@code boolean} values to use as sources of arguments; must not be empty.
	 */
	boolean[] booleans() default {};

	/**
	 * The {@link String} values to use as sources of arguments; must not be empty.
	 */
	String[] strings() default {};

	/**
	 * The {@link Class} values to use as sources of arguments; must not be empty.
	 */
	Class<?>[] classes() default {};

	@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@interface CartesianValueSources {

		CartesianValueSource[] value();

	}

}
