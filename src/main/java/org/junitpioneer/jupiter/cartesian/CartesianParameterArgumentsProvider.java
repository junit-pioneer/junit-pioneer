/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.cartesian;

import java.lang.reflect.Parameter;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Provides arguments for a single parameter of a {@link CartesianTest} method.
 *
 * <p>For more information, see
 * <a href="https://junit-pioneer.org/docs/cartesian-product/" target="_top">the Cartesian product documentation</a>.</p>
 */
public interface CartesianParameterArgumentsProvider<T> extends CartesianArgumentsProvider {

	/**
	 * Provides a {@link Stream} of arguments that needs to be used for a {@link CartesianTest} parameter.
	 *
	 * @param context the current extension context
	 * @param parameter the parameter for which the arguments have to be provided
	 * @return a stream of arguments
	 */
	Stream<T> provideArguments(ExtensionContext context, Parameter parameter) throws Exception;

}
