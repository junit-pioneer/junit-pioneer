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

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junitpioneer.internal.TestNameFormatter;

/**
 * {@code @RetryingTest} is a JUnit Jupiter extension that retries
 * a failing test a certain number of times before the test actually
 * shows up as failing.
 *
 * <p>If annotated with {@code @RetryingTest(n)}, a test method is
 * executed as long as it keeps failing, but no more than {@code n}
 * times. That means all actual executions - except possibly the
 * last - have failed. In contrast, all executions - except possibly
 * the last - show up as being ignored/aborted because that is the best
 * way to communicate a problem without breaking the test suite. Only
 * if all {@code n} executions fail, is the last one marked as such.
 * Each ignored/aborted or failed execution includes the underlying
 * exception.</p>
 *
 * <p>By default the test will be retried on all exceptions except
 * {@link org.opentest4j.TestAbortedException TestAbortedException}
 * (which will abort the test entirely). To only retry on specific
 * exceptions, use {@link RetryingTest#onExceptions() onExceptions()}.</p>
 *
 * <p>{@code @RetryingTest} has a number of limitations:</p>
 *
 * <ul>
 *     <li>it can only be applied to methods</li>
 *     <li>methods annotated with this annotation <b>MUST NOT</b> be annotated with {@code @Test}
 *         to avoid multiple executions!</li>
 *     <li>it can't be used with other {@link TestTemplate}-based mechanisms
 *         like {@code org.junit.jupiter.api.RepeatedTest @RepeatedTest} or
 *         {@code org.junit.jupiter.params.ParameterizedTest @ParameterizedTest}</li>
 *     <li>it can't be used with {@code org.junit.jupiter.api.DynamicTest @DynamicTest}</li>
 *     <li>all retries are run sequentially, even when used with
 *         <a href="https://junit.org/junit5/docs/current/user-guide/#writing-tests-parallel-execution">parallel test execution</a></li>
 * </ul>
 *
 * <p>During
 * <a href="https://junit.org/junit5/docs/current/user-guide/#writing-tests-parallel-execution" target="_top">parallel test execution</a>,
 * all repetitions of a {@code RetryingTest} are executed sequentially to guarantee thread-safety.</p>
 *
 * <p>For more details and examples, see
 * <a href="https://junit-pioneer.org/docs/retrying-test/" target="_top">the documentation on <code>@RetryingTest</code></a>.</p>
 *
 * <p>Before version 0.7.0 this annotation was called {@code @RepeatFailedTest}.</p>
 *
 * @since 0.4
 */
@Target({ METHOD, ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
// the extension is inherently thread-unsafe (has to wait for one execution before starting the next),
// so it forces execution of all retries onto the same thread
@Execution(SAME_THREAD)
@ExtendWith(RetryingTestExtension.class)
@TestTemplate
public @interface RetryingTest {

	/**
	 * Placeholder for the display name of a {@code @RetryingTest}:
	 * <code>{displayName}</code>
	 *
	 * @since 1.7.0
	 *
	 * @see #name
	 */
	String DISPLAY_NAME_PLACEHOLDER = TestNameFormatter.DISPLAY_NAME_PLACEHOLDER;

	/**
	 * Placeholder for the current invocation index of a {@code @RetryingTest}
	 * method (1-based): <code>{index}</code>
	 *
	 * @since 1.7.0
	 *
	 * @see #name
	 */
	String INDEX_PLACEHOLDER = TestNameFormatter.INDEX_PLACEHOLDER;

	/**
	 * The display name to be used for individual invocations of the
	 * parameterized test; never blank or consisting solely of whitespace.
	 *
	 * <p>Defaults to [{index}] {arguments}.</p>
	 *
	 * <p>Supported placeholders:<p>
	 *
	 * - {@link org.junitpioneer.jupiter.RetryingTest#DISPLAY_NAME_PLACEHOLDER}
	 * - {@link org.junitpioneer.jupiter.RetryingTest#INDEX_PLACEHOLDER}
	 *
	 * <p>You may use {@link java.text.MessageFormat} patterns
	 * to customize formatting.</p>
	 *
	 * @since 1.7.0
	 *
	 * @see java.text.MessageFormat
	 * @see org.junit.jupiter.params.ParameterizedTest#name()
	 */
	String name() default "[{index}]";

	/**
	 * Specifies how often the test is executed at most.
	 *
	 * <p>Alias for {@link #maxAttempts()}.</p>
	 */
	int value() default 0;

	/**
	 * Specifies how often the test is executed at most.
	 *
	 * <p>Either this or {@link #value()} are required.
	 * The value must be greater than {@link #minSuccess()}.</p>
	 */
	int maxAttempts() default 0;

	/**
	 * Specifies the minimum number of successful executions of the test.
	 *
	 * <p>The test will be executed at least this number of times. If the test does not complete
	 * successfully the given number of times, the test will fail.</p>
	 *
	 * <p>Value must be greater than or equal to 1.</p>
	 */
	int minSuccess() default 1;

	/**
	 * Specifies on which exceptions a failed test is retried.
	 *
	 * <p>If no exceptions are specified, tests will always be retried; otherwise only when it throws
	 * an exception that can be assigned to one of the specified types.</p>
	 */
	// for the rationale to handle Throwable instead of just Exception, see
	// explanation in org.junit.jupiter.api.function.Executable
	Class<? extends Throwable>[] onExceptions() default {};

}
