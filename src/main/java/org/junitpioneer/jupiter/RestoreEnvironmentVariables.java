/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * {@code @RestoreEnvironmentVariables} is a JUnit Jupiter extension to restore the entire set of
 * environment variables to the original value, or the value of the higher-level container, after the
 * annotated element has been executed.
 *
 * <p>Use this annotation when you need programmatically modify environment variables in a test method
 * or in {@code @BeforeAll} / {@code @BeforeEach} blocks.
 * To simply set or clear an environment variable, consider
 * {@link SetEnvironmentVariable @SetEnvironmentVariable} or
 * {@link ClearEnvironmentVariable @ClearEnvironmentVariable} instead.
 * </p>
 *
 * <p>{@code RestoreEnvironmentVariables} can be used on the method and on the class level.
 * When placed on a test method, environment variables are stored before the method is run and restored
 * after the method is complete. Specifically, variables are stored after any {@code @BeforeAll}
 * methods have run and before any {@code @BeforeEach} methods.</p>
 *
 * <p>When placed on a test class, environment variables are stored before the test class runs and
 * restored after the test class is complete, <em>in addition to</em> running before and after
 * each test method just as if the annotation was on each method. An advanced usage could include
 * modifying some environment variables in a {@code @BeforeAll} block to apply to all tests and
 * additional variable modifications within some tests themselves, all while safely restoring
 * the state of the environment variables after each test and after the entire test class.</p>
 *
 * <p>WARNING: Java considers environment variables to be immutable, so this extension
 * uses reflection to change them. This requires that the {@link SecurityManager}
 * allows modifications and can potentially break on different operating systems and
 * Java versions. Be aware that this is a fragile solution and consider finding a
 * better one for your specific situation. If you're running on Java 9 or later and
 * are encountering warnings or errors, check
 * <a href="https://junit-pioneer.org/docs/environment-variables/#warnings-for-reflective-access">the documentation</a>.</p>
 *
 * <p>{@code SetEnvironmentVariable} and {@code ClearEnvironmentVariable} interaction: During
 * <a href="https://junit.org/junit5/docs/current/user-guide/#writing-tests-parallel-execution" target="_top">parallel test execution</a>,
 * all tests annotated with {@link RestoreEnvironmentVariables}, {@link SetEnvironmentVariable},
 * {@link ReadsEnvironmentVariable}, and {@link WritesEnvironmentVariable}
 * are scheduled in a way that guarantees correctness under mutation of shared global state.
 * </p>
 *
 * <p>For more details and examples, see
 * <a href="https://junit-pioneer.org/docs/environment-variables/" target="_top">the documentation on
 * <code>@ClearEnvironmentVariable, @SetEnvironmentVariable and @RestoreEnvironmentVariables</code></a>.
 * </p>
 *
 * @since 2.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Inherited
@WritesEnvironmentVariable
@ExtendWith(EnvironmentVariableExtension.class)
public @interface RestoreEnvironmentVariables {
}
