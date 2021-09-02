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

/**
 * {@code @RetryingTest} is a JUnit Jupiter extension that retries
 * a failing test a certain number of times before the test actually
 * shows up as failing.
 *
 * If annotated with {@code @RetryingTest(n)}, a test method is
 * executed as long as it keeps failing, but no more than {@code n}
 * times. That means all actual executions - except possibly the
 * last - have failed. In contrast, all executions - except possibly
 * the last - show up as being ignored/aborted because that is the best
 * way to communicate a problem without breaking the test suite. Only
 * if all {@code n} executions fail, is the last one marked as such.
 * Each ignored/aborted or failed execution includes the underlying
 * exception.
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
 * all repetitions of a {@code @RepeatFailedTest} are executed sequentially to guarantee thread-safety.
 * </p>
 *
 * <p>For more details and examples, see
 * <a href="https://junit-pioneer.org/docs/repeat-failed-test/" target="_top">the documentation on <code>@RepeatFailedTest</code></a>.
 * </p>
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
	 * Specifies how often the test is executed at most.
	 *
	 * Alias for {@link #maxAttempts()}.
	 */
	int value() default 0;

	/**
	 * Specifies how often the test is executed at most.
	 *
	 * Either this or {@link #value()} are required.
	 * The value must be greater than {@link #minSuccess()}.
	 */
	int maxAttempts() default 0;

	/**
	 * Specifies the minimum number of successful executions of the test.
	 *
	 * The test will be executed at least this number of times. If the test does not complete
	 * successfully the given number of times, the test will fail.
	 *
	 * Value must be greater than or equal to 1.
	 */
	int minSuccess() default 1;

	/**
	 * Specifies on which exceptions a failed test is retried.
	 *
	 * If no exceptions are specified, tests will always be retried; otherwise only when it throws
	 * an exception that can be assigned to one of the specified types.
	 */
	// for the rationale to handle Throwable instead of just Exception, see
	// explanation in org.junit.jupiter.api.function.Executable
	Class<? extends Throwable>[] onExceptions() default {};

}
