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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provide values that the {@link StdIOExtension extension} will read instead of reading the
 * standard input ({@code System.in}).
 * This should be used with {@link org.junitpioneer.jupiter.StdIOExtension.StdIn}.
 *
 * <p>This extension is not safe to use during
 * <a href="https://junit.org/junit5/docs/current/user-guide/#writing-tests-parallel-execution" target="_top">parallel test execution</a>.
 * (We're working on it.)
 * </p>
 *
 * <p>For more details and examples, see
 * <a href="https://junit-pioneer.org/docs/standard-input-output/" target="_top">the documentation on <code>Standard input/output</code></a>.
 * </p>
 *
 * @since 0.7
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface StdInSource {

	/**
	 * Provides the intercepted standard input with values.
	 */
	String[] value();

}
