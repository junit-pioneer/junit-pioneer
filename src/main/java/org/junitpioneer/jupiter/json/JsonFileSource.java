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
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.params.provider.ArgumentsSource;

/**
 * {@code @JsonSource} is an {@link ArgumentsSource} that parses the json and passes arguments to the parametrized test.
 *
 *
 * <p>This annotation is {@link Repeatable}, to make it usable with {@link org.junitpioneer.jupiter.CartesianProductTest}.
 * If used with {@link org.junit.jupiter.params.ParameterizedTest}, it can only be used once (because {@code ParameterizedTest}
 * can only take a single {@link ArgumentsSource}). Using it more than once will throw an {@link IllegalArgumentException}.
 * If used with {@link org.junitpioneer.jupiter.CartesianProductTest}, it can be repeated to provide arguments to
 * more than one parameter.
 * </p>
 *
 * <p>
 * For more details and examples, see
 * <a href="https://junit-pioneer.org/docs/json/" target="_top">the documentation on <code>JSON tests</code></a>
 * </p>
 *
 * @see ArgumentsSource
 * @see org.junit.jupiter.params.ParameterizedTest
 * @see org.junitpioneer.jupiter.CartesianProductTest
 * @since TBD
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ArgumentsSource(JsonFileArgumentsProvider.class)
@Repeatable(JsonFileSource.JsonSources.class)
public @interface JsonFileSource {

	/**
	 * The JSON classpath resources to use as the sources of arguments; must not
	 * be empty unless {@link #files} is non-empty.
	 */
	String[] resources() default {};

	/**
	 * The JSON files to use as the sources of arguments; must not be empty
	 * unless {@link #resources} is non-empty.
	 */
	String[] files() default {};

	/**
	 * The name of the element from which the data should be extracted from.
	 * If not set the root element will be used.
	 */
	String data() default "";

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@interface JsonSources {

		JsonFileSource[] value();

	}

}
