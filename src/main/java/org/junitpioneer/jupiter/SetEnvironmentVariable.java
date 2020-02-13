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
 * {@code @SetEnvironmentVariable} is a JUnit Jupiter extension to set the value of a
 * environment variable for a test execution.
 *
 * <p>The key and value of the environment variable to be set must be specified via
 * {@link #key()} and {@link #value()}. After the annotated method has been
 * executed, the initial default value is restored.
 *
 * <p>{@code SetEnvironmentVariable} is repeatable and can be used on the method and on
 * the class level. If a class is annotated, the configured variable will be set
 * for all tests inside that class. Any method level configurations will
 * override the class level configurations.
 *
 * <p>Warning: If your {@link SecurityManager} does not allow modifications, it fails.
 *
 * @since 0.6
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Repeatable(SetEnvironmentVariables.class)
@ExtendWith(EnvironmentVariableExtension.class)
public @interface SetEnvironmentVariable {

	/**
	 * The key of the system property to be set.
	 */
	String key();

	/**
	 * The value of the system property to be set.
	 */
	String value();

}
