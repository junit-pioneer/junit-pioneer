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

import java.lang.reflect.Parameter;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * If you are implementing an {@link org.junit.jupiter.params.provider.ArgumentsProvider ArgumentsProvider}
 * for {@link CartesianTest}, it has to implement this interface <b>as well</b> to know which parameter it provides
 * arguments to. For more information, see
 * <a href="https://junit-pioneer.org/docs/cartesian-product/" target="_top">the Cartesian product documentation</a>.
 *
 * @param <T> type of arguments this provider returns
 *
 * @see org.junit.jupiter.params.provider.ArgumentsProvider
 * @see CartesianTestExtension
 */
public interface CartesianArgumentsProvider<T> {

	/**
	 * Provider a {@link Stream} of values that needs to be used for a single parameter in {@code @CartesianTest}.
	 *
	 * @param context the current extension context; never {@code null}
	 * @param parameter the parameter for which the arguments needs to be provided
	 * @return a stream of values; never {@code null}
	 */
	Stream<T> provideArguments(ExtensionContext context, Parameter parameter) throws Exception;

}
