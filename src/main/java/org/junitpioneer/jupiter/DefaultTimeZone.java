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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * {@code @DefaultTimeZone} is a JUnit Jupiter extension to change the value
 * returned by {@link java.util.TimeZone#getDefault()} for a test execution.
 *
 * <p>The {@link java.util.TimeZone} to set as the default {@code TimeZone} is
 * configured by specifying the {@code TimeZone} ID as defined by
 * {@link java.util.TimeZone#getTimeZone(String)}. After the annotated element
 * has been executed, the default {@code TimeZone} will be restored to its
 * original value.</p>
 *
 * <p>{@code @DefaultTimeZone} can be used on the method and on the class
 * level. If a class is annotated, the configured {@code TimeZone} will be the
 * default {@code TimeZone} for all tests inside that class. Any method level
 * configurations will override the class level default {@code TimeZone}.</p>
 *
 * <p>During
 * <a href="https://junit.org/junit5/docs/current/user-guide/#writing-tests-parallel-execution" target="_top">parallel test execution</a>,
 * all tests annotated with {@link DefaultTimeZone}, {@link ReadsDefaultTimeZone}, and {@link WritesDefaultTimeZone}
 * are scheduled in a way that guarantees correctness under mutation of shared global state.
 * </p>
 *
 * <p>For more details and examples, see
 * <a href="https://junit-pioneer.org/docs/default-locale-timezone/" target="_top">the documentation on <code>@DefaultLocale and @DefaultTimeZone</code></a>.
 * </p>
 *
 * @since 0.2
 * @see java.util.TimeZone#getDefault()
 * @see DefaultLocale
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@WritesDefaultTimeZone
@ExtendWith(DefaultTimeZoneExtension.class)
public @interface DefaultTimeZone {

	/**
	 * The ID for a {@code TimeZone}, either an abbreviation such as "PST", a
	 * full name such as "America/Los_Angeles", or a custom ID such as
	 * "GMT-8:00". Note that the support of abbreviations is for JDK 1.1.x
	 * compatibility only and full names should be used.
	 */
	String value();

}
