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

import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.parallel.Execution;

/**
 * Provide values that the {@link StdIOExtension extension} will read instead of reading the
 * standard input ({@code System.in}).
 * This should be used with {@link org.junitpioneer.jupiter.StdIOExtension.StdIn}.
 *
 * <p>The extension will run single threaded to guarantee thread safety!</p>
 */
@Execution(SAME_THREAD)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface StdInSource {

	/**
	 * Provides the intercepted standard input with values.
	 */
	String[] value();

}
