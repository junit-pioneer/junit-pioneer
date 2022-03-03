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
 * {@code @CartesianArgumentsSource} is an annotation
 * that is used to register cartesian argument providers
 * for the annotated test parameter in case of {@link CartesianParameterArgumentsProvider}
 * or for all the test parameters in case of {@link CartesianMethodArgumentsProvider}.
 *
 * <p>{@code @CartesianArgumentsSource} may also be used as a meta-annotation in order to
 * create a custom <em>composed annotation</em> that inherits the semantics
 * of {@code @CartesianArgumentsSource}.
 *
 * This annotation is used to provide arguments for a {@link CartesianTest}.
 *
 * @see CartesianTest
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CartesianArgumentsSource {

	/**
	 * The type of {@link CartesianArgumentsProvider} to be used.
	 */
	@SuppressWarnings("rawtypes")
	Class<? extends CartesianArgumentsProvider> value();

}
