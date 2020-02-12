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

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

/**
 * {@code @Stopwatch} is a JUnit Jupiter extension measure the elapsed time of a test execution.
 * It's based on the JUnit extension example.
 *
 * <p>{@code Stopwatch} is not repeatable and can be used on the method and class level.
 *
 * @since 0.6
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@ExtendWith(StopwatchExtension.class)
public @interface Stopwatch {

}
