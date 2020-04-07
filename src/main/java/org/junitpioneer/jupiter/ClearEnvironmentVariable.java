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
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * {@code @ClearEnvironmentVariable} is a JUnit Jupiter extension to clear the value
 * of a environment variable for a test execution.
 *
 * <p>The key of the environment variable to be cleared must be specified via
 * {@link #key()}. After the annotated element has been executed, After the
 * annotated method has been executed, the initial default value is restored.
 *
 * <p>{@code ClearEnvironmentVariable} is repeatable and can be used on the method and
 * on the class level. If a class is annotated, the configured variable will be
 * cleared for all tests inside that class.
 *
 * <p>Warning: If your {@link SecurityManager} does not allow modifications, it fails.
 *
 * @since 0.6
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Repeatable(ClearEnvironmentVariables.class)
@ExtendWith(EnvironmentVariableExtension.class)
public @interface ClearEnvironmentVariable {

	/**
	 * The key of the environment variable to be cleared.
	 */
	String key();

}
