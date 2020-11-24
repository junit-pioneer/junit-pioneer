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

import static java.util.stream.Collectors.toList;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * {@code @CartesianProductTest} is a JUnit Jupiter extension that marks
 * a test to be executed with all possible input combinations.
 *
 * <p>Methods annotated with this annotation should not be annotated with {@code Test}.
 * </p>
 *
 * <p>This annotation is somewhat similar to {@code @ParameterizedTest}, as in it also takes
 * arguments and can run the same test multiple times. With {@code @CartesianProductTest} you
 * don't specify the test cases themselves. though. Instead you specify possible values for
 * each test method parameter (see @{@link CartesianValueSource}) and the extension runs the
 * method with each possible combination.
 * </p>
 *
 * <p>You can specify a custom Display Name for the tests ran by {@code @CartesianProductTest}.
 * By default it's [{index}] {arguments}.
 * </p>
 *
 * <p>For more details and examples, see
 * <a href="https://junit-pioneer.org/docs/cartesian-product/" target="_top">the documentation on <code>@CartesianProductTest</code></a>.
 * </p>
 * @see org.junitpioneer.jupiter.CartesianValueSource
 */
@TestTemplate
@ExtendWith(CartesianProductTestExtension.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CartesianProductTest {

	/**
	 *
	 */
	String name() default "[{index}] {arguments}";

	/**
	 * Specifies {@code String} values for all inputs simultaneously.
	 */
	String[] value() default {};

	/**
	 * Specifies the name of the method that supplies the {@code Sets} for the test.
	 */
	String factory() default "";

	class Sets {

		private final List<List<?>> sets = new ArrayList<>(); //NOSONAR

		public Sets add(Object... entries) {
			return addAll(Arrays.stream(entries));
		}

		public Sets addAll(Iterable<?> entries) {
			return addAll(StreamSupport.stream(entries.spliterator(), false));
		}

		public Sets addAll(Collection<?> entries) {
			return addAll(entries.stream());
		}

		public Sets addAll(Stream<?> entries) {
			sets.add(entries.distinct().collect(toList()));
			return this;
		}

		List<List<?>> getSets() { //NOSONAR
			return sets;
		}

	}

}
