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
 * {@code @SetSystemProperty} is a JUnit Jupiter extension to set the value
 * of a system property for a test execution.
 *
 * <p>The key and value of the system property to be set must be specified
 * via {@link #key()} and {@link #value()}. After the annotated method has
 * been executed, all system properties will be restored to their original
 * value.</p>
 *
 * @since 0.4
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Repeatable(SetSystemProperties.class)
@ExtendWith(SystemPropertyExtension.class)
public @interface SetSystemProperty {

	/**
	 * The key of the system property to be set.
	 */
	String key();

	/**
	 * The value of the system property to be set.
	 */
	String value();

}
