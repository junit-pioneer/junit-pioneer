/*
 * Copyright 2016-2022 the original author or authors.
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
 * {@code @Stopwatch} is a JUnit Jupiter extension to measure the elapsed time of a test execution.
 * It's based on the JUnit extension example.
 *
 * <p>{@code Stopwatch} is not repeatable. It can be used on the method and class level.</p>
 *
 * @since 0.6
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Inherited
@ExtendWith(StopwatchExtension.class)
public @interface Stopwatch {

}
