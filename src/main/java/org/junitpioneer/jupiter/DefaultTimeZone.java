/*
 * Copyright 2015-2018 the original author or authors.
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
 * <p>The {@link java.util.TimeZone} to set as the default TimeZone is
 * configured bei specifying the TimeZone ID as defined by
 * {@link java.util.TimeZone#getTimeZone(String)}. After the annotated element
 * has been executed, the default TimeZone will be restored to its original
 * value.</p>
 *
 * <p>{@code @DefaultTimeZone} can be used on the method and on the class
 * level. If a class is annotated, the configured TimeZone will be the default
 * TimeZone for all tests inside that class. Any method level configurations
 * will override the class level default TimeZone.</p>
 *
 * @since 0.2
 * @see java.util.TimeZone#getDefault()
 * @see DefaultLocale
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DefaultTimeZoneExtension.class)
public @interface DefaultTimeZone {

	String value();
}
