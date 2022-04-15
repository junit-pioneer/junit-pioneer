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
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * {@code @DisabledUntil} is a JUnit Jupiter extension to mark tests that shouldn't be executed until a given date,
 * essentially disabling a test temporarily. The date is given as an ISO 8601 string.
 *
 * <p>It may optionally be declared with a reason to document why the annotated test class or test
 * method is disabled.
 *
 * <p>{@code @DisabledUntil} can be used on the method and class level. It can only be used once per method or class,
 * but is inherited from higher-level containers.</p>
 *
 * @see org.junit.jupiter.api.Disabled
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ExtendWith(DisabledUntilExtension.class)
public @interface DisabledUntil {

	/**
	 * The reason this annotated test class or test method is disabled.
	 */
	String reason() default "";

	/**
	 * The date until which this annotated test class or test method should be disabled as an ISO 8601 string in the
	 * format yyyy-MM-dd, e.g. 2023-05-28.
	 * The test will be disabled if that date is in the future, as in not today nor any day in the past.
	 */
	String date();

}
