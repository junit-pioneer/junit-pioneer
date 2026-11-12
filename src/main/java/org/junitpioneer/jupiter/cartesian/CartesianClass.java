/*
 * Copyright 2016-2026 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.cartesian;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.ClassTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Executes a test class for every combination of its constructor parameter sources.
 *
 * <p>Annotate every constructor parameter with {@link CartesianTest.Values},
 * {@link CartesianTest.Enum}, or a Cartesian parameter arguments source such as a range source.
 * All test methods run for each combination with Jupiter's default PER_METHOD test instance lifecycle.
 * Constructor injection with PER_CLASS lifecycle is not supported.
 * Method-level {@link CartesianTest} tests remain independent.</p>
 *
 * <p>The class must be top-level or static nested and declare a single constructor.
 * Every constructor parameter must have exactly one arguments source.
 * Field injection, non-static nested Cartesian classes, and class-level factory sources are not supported.
 * For more information, see
 * <a href="https://junit-pioneer.org/docs/cartesian-class/" target="_top">the Cartesian product documentation</a>.</p>
 *
 * @since 3.0.0
 */
@ClassTemplate
@ExtendWith(CartesianClassExtension.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface CartesianClass {

	/**
	 * Display name for each class invocation. Supports {@code {displayName}},
	 * {@code {index}}, {@code {arguments}}, and individual arguments such as {@code {0}}.
	 * Must not be blank.
	 */
	String name() default "[{index}] {arguments}";

}
