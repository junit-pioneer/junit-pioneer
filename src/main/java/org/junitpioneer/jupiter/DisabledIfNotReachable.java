/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @DisabledIfNotReachable} is a JUnit Jupiter extension to check if a given URL is reachable before executing
 * the test and disable the test if it's not.
 *
 * <p>A maximum timeout (in milliseconds) can be passed as a second argument. The default value is 5000.</p>
 *
 * <p>{@code @DisabledIfNotReachable} can be used on the method and class level. It can only be used once per method or class,
 * but is inherited from higher-level containers.</p>
 *
 * @since 2.3.0
 * @see org.junit.jupiter.api.Disabled
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Inherited
@ExtendWith(DisabledIfNotReachableExtension.class)
public @interface DisabledIfNotReachable {

	/**
	 * The url to be checked.
	 */
	String url() default "";

	/**
	 * Timeout in milliseconds the lookup is allowed to take at a maximum.
	 */
	int timeoutMillis() default 5000;

}
