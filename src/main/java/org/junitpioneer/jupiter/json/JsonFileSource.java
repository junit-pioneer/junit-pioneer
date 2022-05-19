/*
 * Copyright 2016-2022 the original author or authors.
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
 * {@code @JsonFileSource} is an {@link ArgumentsSource} that loads JSON from a
 * file, parses it, and passes it as arguments to a parametrized test.
 *
 * <p>This annotation can be used on a method parameter, to make it usable with
 * {@link org.junitpioneer.jupiter.cartesian.CartesianTest}.
 * If used with {@link org.junit.jupiter.params.ParameterizedTest},
 * the annotation has to be on the method itself as any other {@link ArgumentsSource}.
 * </p>
 *
 * <p>Note that this extension requires a JSON parser to be available at run time,
 * which may include adding it to the module graph with {@code --add-modules}.
 * For details on that as well as how to use this extension, see
 * <a href="https://junit-pioneer.org/docs/json-argument-source" target="_top">the documentation on <code>JSON tests</code></a>
 * </p>
 *
 * @since 1.7.0
 *
 * @see ArgumentsSource
 * @see org.junit.jupiter.params.ParameterizedTest ParameterizedTest
 * @see org.junitpioneer.jupiter.cartesian.CartesianTest CartesianTest
 * @see Property
 * @see JsonSource
 * @see JsonClasspathSource
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ArgumentsSource(JsonFileSourceArgumentsProvider.class)
public @interface JsonFileSource {

	/**
	 * The JSON files to use as the sources of arguments; must not be empty.
	 */
	String[] value() default {};

	/**
	 * The name of the element from which the data should be extracted from.
	 * If not set the root element will be used.
	 */
	String data() default "";

}
