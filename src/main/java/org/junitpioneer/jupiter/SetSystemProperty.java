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
 * {@code @SetSystemProperty} is a JUnit Jupiter extension to set the value of a
 * system property for a test execution.
 *
 * <p>The key and value of the system property to be set must be specified via
 * {@link #key()} and {@link #value()}. After the annotated method has been
 * executed, the initial default value is restored.
 *
 * <p>{@code SetSystemProperty} is repeatable and can be used on the method and on
 * the class level. If a class is annotated, the configured property will be set
 * for all tests inside that class. Any method level configurations will
 * override the class level configurations.
 *
 * @since 0.5
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Repeatable(SetSystemProperty.SetSystemProperties.class)
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

	/**
	 * Containing annotation of repeatable {@code @SetSystemProperty}.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.METHOD, ElementType.TYPE })
	@ExtendWith(SystemPropertyExtension.class)
	@interface SetSystemProperties {

		SetSystemProperty[] value();

	}

}
