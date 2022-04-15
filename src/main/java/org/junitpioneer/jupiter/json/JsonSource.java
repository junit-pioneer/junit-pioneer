/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.json;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.params.provider.ArgumentsSource;

/**
 * {@code @JsonSource} is an {@link ArgumentsSource} that parses inline JSON
 * and passes it as arguments to a parametrized test.
 *
 * <p>This annotation can be used on a method parameter, to make it usable with
 * {@link org.junitpioneer.jupiter.cartesian.CartesianTest}.
 * If used with {@link org.junit.jupiter.params.ParameterizedTest},
 * the annotation has to be on the method itself as any other {@link ArgumentsSource}.
 * </p>
 * <p>
 * For more details and examples, see
 * <a href="https://junit-pioneer.org/docs/json/" target="_top">the documentation on <code>JSON tests</code></a>
 * </p>
 *
 * @since 1.7.0
 *
 * @see ArgumentsSource
 * @see org.junit.jupiter.params.ParameterizedTest ParameterizedTest
 * @see org.junitpioneer.jupiter.cartesian.CartesianTest CartesianTest
 * @see Property
 * @see JsonFileSource
 * @see JsonClasspathSource
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ArgumentsSource(JsonInlineArgumentsProvider.class)
public @interface JsonSource {

	/**
	 * The JSON values to use as the source of arguments; must not be empty.
	 * <p>
	 * Each value can represent a single object, or a collection of objects.
	 */
	String[] value();

}
