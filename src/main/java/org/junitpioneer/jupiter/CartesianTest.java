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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * {@code @CartesianTest} is a JUnit Jupiter extension that marks
 * a test to be executed with all possible input combinations.
 *
 * <p>Methods annotated with this annotation should not be annotated with {@code Test}.
 * </p>
 *
 * <p>This annotation is somewhat similar to {@code @ParameterizedTest}, as in it also takes
 * arguments and can run the same test multiple times. With {@code @CartesianTest} you
 * don't specify the test cases themselves, though. Instead you specify possible values for
 * each test method parameter (see @{@link CartesianValueSource}) by annotating the parameters
 * themselves and the extension runs the method with each possible combination.
 * </p>
 *
 * <p>You can specify a custom Display Name for the tests ran by {@code @CartesianTest}.
 * By default it's [{index}] {arguments}.
 * </p>
 *
 * <p>For more details and examples, see
 * <a href="https://junit-pioneer.org/docs/cartesian-product/" target="_top">the documentation on <code>@CartesianTest</code></a>.
 * </p>
 * @see org.junitpioneer.jupiter.CartesianValueSource
 *
 * @since 1.5.0
 */
@TestTemplate
@ExtendWith(CartesianTestExtension.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CartesianTest {

	/**
	 * <p>The display name to be used for individual invocations of the
	 * parameterized test; never blank or consisting solely of whitespace.
	 * </p>
	 *
	 * <p>Defaults to {@link org.junit.jupiter.params.ParameterizedTest#DEFAULT_DISPLAY_NAME}.
	 * </p>
	 *
	 * Supported placeholders:
	 *
	 * - {@link org.junit.jupiter.params.ParameterizedTest#DISPLAY_NAME_PLACEHOLDER}
	 * - {@link org.junit.jupiter.params.ParameterizedTest#INDEX_PLACEHOLDER}
	 * - {@link org.junit.jupiter.params.ParameterizedTest#ARGUMENTS_PLACEHOLDER}
	 * - <code>{0}</code>, <code>{1}</code>, etc.: an individual argument (0-based)
	 *
	 * <p>For the latter, you may use {@link java.text.MessageFormat} patterns
	 * to customize formatting.
	 * </p>
	 *
	 * @see java.text.MessageFormat
	 * @see org.junit.jupiter.params.ParameterizedTest#name()
	 */
	String name() default "[{index}] {arguments}";

}
