/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Disables all remaining tests in a container if one of them failed.
 *
 * By default, all exceptions (including assertions, but exempting failed assumptions) will lead to
 * disabling the remaining tests. To configure this in more detail, see {@link #with()} and
 * {@link #onAssertion()}.
 *
 * This annotation can be (meta-)present on classes that contain a nested class. In that case, a
 * failing test in the outer class will disable the nested class (if it runs later) and vice versa.
 *
 * This annotation can be (meta-)present on a class and/or its super types (classes or interfaces).
 * In that case, the exception types given to {@link #with()} are merged and {@link #onAssertion()}
 * is or'ed.
 *
 * But if a test fails in a specific class, only other tests in the corresponding container will
 * be disabled. That means if...
 *
 * <ul>
 *     <li>a class {@code SpecificTests} implements interface {@code Tests} and</li>
 *     <li>{@code Tests} is annotated with {@code @DisableIfTestFails} and</li>
 *     <li>a test in {@code SpecificTests} fails</li>
 * </ul>
 *
 * ... then, only remaining tests in {@code SpecificTests} are disabled and other implementations
 * of {@code Tests} remain unaffected, i.e. their tests are not disabled.
 */
@Target({ TYPE, ANNOTATION_TYPE })
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DisableIfTestFailsExtension.class)
public @interface DisableIfTestFails {

	/**
	 * Configure on which exceptions remaining tests are disabled (defaults to "any exception").
	 *
	 * If {@code @DisableIfTestFails} is present multiple times (e.g. on multiple super types),
	 * these exceptions are collected across all annotations, meaning if any of the mentioned
	 * exceptions are thrown, the remaining tests are disabled.
	 */
	Class<? extends Throwable>[] with() default {};

	/**
	 * Set to {@code false} if failed assertions should not lead to disabling remaining tests.
	 *
	 * If {@code @DisableIfTestFails} is present multiple times (e.g. on multiple super types),
	 * this value is or'ed, meaning as soon as one annotation says to fail on assertion errors,
	 * that's how the container will behave.
	 */
	boolean onAssertion() default true;

}
