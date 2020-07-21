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
 * Allows specifying the input that's read from {@code System.in} as well as capturing
 * lines read from {@code System.in} (with parameter {@link StdIn}) or
 * written to {@code System.out} (with parameter {@link StdOut StdOut}).
 *
 * The annotated test method can have zero, one, or both parameters, but {@code StdIn} can only
 * be provided if {@link StdIo#value()} is used to specify input - otherwise an
 * {@link org.junit.jupiter.api.extension.ExtensionConfigurationException ExtensionConfigurationException}
 * will be thrown.
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
