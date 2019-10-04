/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * {@code @RepeatFailedTest} is a JUnit Jupiter extension that repeats
 * a failing test a certain number of times before the test actually
 * shows up as failing.
 *
 * If annotated with {@code @RepeatFailedTest(n)}, a test method is
 * executed as long as it keeps failing, but no more than {@code n}
 * times. That means all actual executions - except possibly the
 * last - have failed. In contrast, all executions - except possibly
 * the last - always show up as having passed because that is the only
 * way not to break the test suite. Only if all {@code n} executions
 * fail, is the last one marked as such and lists all exceptions thrown
 * by previous executions.
 *
 * <p>{@code @RepeatFailedTest} has a number of limitations:
 *
 * <ul>
 *     <li>it can only be applied to methods</li>
 *     <li>it can't be used with other {@link TestTemplate}-based mechanisms
 *         like {@code org.junit.jupiter.api.RepeatedTest @RepeatedTest} or
 *         {@code org.junit.jupiter.params.ParameterizedTest @ParameterizedTest}</li>
 *     <li>it can't be used with
 *         {@code org.junit.jupiter.api.DynamicTest @DynamicTest}</li>
 *     <li>it isn't thread-safe and if used with
 *         <a href="https://junit.org/junit5/docs/current/user-guide/#writing-tests-parallel-execution">parallel test execution</a>,
 *         its effect is undefined</li>
 * </ul>
 *
 * @since 0.4
 */
@Target({ METHOD, ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(RepeatFailedTestExtension.class)
@TestTemplate
public @interface RepeatFailedTest {

	/**
	 * Specifies how often the test is executed at most.
	 */
	int value();

	LogLevel logFailedTestOn() default LogLevel.OFF;

	enum LogLevel {

		/**
		 * @see  java.util.logging.Level#OFF
		 */
		OFF,

		/**
		 * @see  java.util.logging.Level#FINER
		 */
		FINER,

		/**
		 * @see  java.util.logging.Level#FINE
		 */
		FINE,

		/**
		 * @see  java.util.logging.Level#CONFIG
		 */
		CONFIG,

		/**
		 * @see  java.util.logging.Level#INFO
		 */
		INFO,

		/**
		 * @see  java.util.logging.Level#WARNING
		 */
		WARNING,

		/**
		 * @see  java.util.logging.Level#SEVERE
		 */
		SEVERE;

	}

}
