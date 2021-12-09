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
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * {@code @ClearEnvironmentVariable} is a JUnit Jupiter extension to clear the value
 * of a environment variable for a test execution.
 *
 * <p>The key of the environment variable to be cleared must be specified via {@link #key()}.
 * After the annotated element has been executed, the initial default value is restored.</p>
 *
 * <p>{@code ClearEnvironmentVariable} is repeatable and can be used on the method and
 * on the class level. If a class is annotated, the configured variable will be
 * cleared for all tests inside that class.</p>
 *
 * <p>WARNING: Java considers environment variables to be immutable, so this extension
 * uses reflection to change them. This requires that the {@link SecurityManager}
 * allows modifications and can potentially break on different operating systems and
 * Java versions. Be aware that this is a fragile solution and consider finding a
 * better one for your specific situation. If you're running on Java 9 or later, you
 * may have to add {@code --add-opens=java.base/java.util=ALL-UNNAMED} to your test
 * execution to prevent warnings or even errors.</p>
 *
 * <p>During
 * <a href="https://junit.org/junit5/docs/current/user-guide/#writing-tests-parallel-execution" target="_top">parallel test execution</a>,
 * all tests annotated with {@link ClearEnvironmentVariable}, {@link SetEnvironmentVariable}, {@link ReadsEnvironmentVariable}, and {@link WritesEnvironmentVariable}
 * are executed sequentially to guarantee correctness under mutation of shared global state.
 * </p>
 *
 * <p>For more details and examples, see
 * <a href="https://junit-pioneer.org/docs/environment-variables/" target="_top">the documentation on <code>@ClearEnvironmentVariable and @SetEnvironmentVariable</code></a>.
 * </p>
 *
 * @since 0.6
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Inherited
@Repeatable(ClearEnvironmentVariable.ClearEnvironmentVariables.class)
@WritesEnvironmentVariable
@ExtendWith(EnvironmentVariableExtension.class)
public @interface ClearEnvironmentVariable {

	/**
	 * The key of the environment variable to be cleared.
	 */
	String key();

	/**
	 * Containing annotation of repeatable {@code @ClearEnvironmentVariable}.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.METHOD, ElementType.TYPE })
	@Inherited
	@WritesEnvironmentVariable
	@ExtendWith(EnvironmentVariableExtension.class)
	@interface ClearEnvironmentVariables {

		ClearEnvironmentVariable[] value();

	}

}
