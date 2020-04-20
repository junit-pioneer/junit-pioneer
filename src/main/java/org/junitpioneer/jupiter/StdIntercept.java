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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Intercept communication on the standard input/output,
 * to verify behaviour. This should be used with
 * {@link org.junitpioneer.jupiter.StdIOExtension.StdOut}
 * to intercept the standard output ({@code System.in}) and
 * {@link org.junitpioneer.jupiter.StdIOExtension.StdIn}
 * to intercept the standard input ({@code System.out}).
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface StdIntercept {

	/**
	 * Provides the intercepted standard input with values.
	 * Ignored when intercepting the standard output.
	 * Defaults to empty string.
	 *
	 * Please note that you should always supply values when intercepting the standard input!
	 * This is not enforced because you might want to test with empty input.
	 */
	String[] value() default "";

}
