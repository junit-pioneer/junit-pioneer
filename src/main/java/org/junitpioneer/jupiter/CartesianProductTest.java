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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * {@code @CartesianProductTest} is a JUnit Jupiter extension that marks
 * a test to be executed with all possible input combinations.
 *
 * <p>Methods annotated with this annotation <b>MUST NOT</b> be annotated with {@code Test},
 * because it will throw an exception.
 * </p>
 *
 * <p>Methods annotated with this annotation are different from {@code ParameterizedTest}s because
 * they can not have {@code ArgumentsSource}s - those are completely disregarded. Instead a
 * String array must be provided (for methods with only String parameters) or a static factory method
 * with the same name as the test method must exist.
 * </p>
 *
 * @since ???
 */
@TestTemplate
@ExtendWith(CartesianProductTestExtension.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CartesianProductTest {

	/**
	 * Specifies {@code String} values for all inputs simultaneously.
	 */
	String[] value() default {};

	String factory() default "";

	class Sets {

		private final List<List<?>> sets = new ArrayList<>(); //NOSONAR

		public Sets add(Object... entries) {
			sets.add(new ArrayList<>(Arrays.asList(entries)));
			return this;
		}

		List<List<?>> getSets() { //NOSONAR
			return sets;
		}

	}

}
