/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.cartesian;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @CartesianArgumentsSources} is a simple container for one or more
 * {@link CartesianArgumentsSource} annotations.
 *
 * <p>Note, however, that use of the {@code @CartesianArgumentsSources} container is completely
 * optional since {@code @CartesianArgumentsSource} is a {@linkplain java.lang.annotation.Repeatable
 * repeatable} annotation.
 *
 * @since 5.0
 * @see CartesianArgumentsSource
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CartesianArgumentsSources {

	/**
	 * An array of one or more {@link CartesianArgumentsSource @CartesianArgumentsSource}
	 * annotations.
	 */
	CartesianArgumentsSource[] value();

}
