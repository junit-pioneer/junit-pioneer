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

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Marks a method to be extended with {@link StdIoExtension} and provided with
 * {@link StdIn StdIn} or {@link StdOut StdOut}.
 * The annotated method must take one or both parameters or an
 * {@link org.junit.jupiter.api.extension.ExtensionConfigurationException} will be thrown.
 *
 * Provide values that the {@link StdIoExtension extension} will read instead of reading the
 * standard input ({@code System.in}).
 * If values are provided but there is no {@link StdIn} parameter
 * an {@link org.junit.jupiter.api.extension.ExtensionConfigurationException} will be thrown.
 * The opposite is not true to enable testing for empty inputs.
 *
 * <p>For more details and examples, see
 * <a href="https://junit-pioneer.org/docs/standard-input-output/" target="_top">the documentation on <code>Standard input/output</code></a>.
 * </p>
 *
 * @since 0.7
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ExtendWith(StdIoExtension.class)
public @interface StdIo {

	/**
	 * Provides the intercepted standard input with values.
	 * If this is not blank, the annotated method can
	 * have a {@link StdIn} parameter.
	 */
	String[] value() default {};

}
