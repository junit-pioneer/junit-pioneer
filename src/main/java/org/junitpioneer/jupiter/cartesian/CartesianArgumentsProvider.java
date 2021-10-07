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
import org.junit.jupiter.params.provider.Arguments;

/**
 * If you are implementing an {@link org.junit.jupiter.params.provider.ArgumentsProvider ArgumentsProvider}
 * for {@link CartesianTest}, it has to implement this interface <b>as well</b> to know which parameter it provides
 * arguments to. For more information, see
 * <a href="https://junit-pioneer.org/docs/cartesian-product/" target="_top">the Cartesian product documentation</a>.
 *
 * @see org.junit.jupiter.params.provider.ArgumentsProvider
 * @see CartesianTestExtension
 */
public interface CartesianArgumentsProvider {

	/**
	 * Provider a {@link Stream} of {@link Arguments} that needs to be used for the {@code @CartesianTest}.
	 *
	 * @param context the current extension context; never {@code null}
	 * @param parameter the parameter for which the arguments needs to be provided
	 * @return a stream of arguments; never {@code null}
	 */
	Stream<? extends Arguments> provideArguments(ExtensionContext context, Parameter parameter) throws Exception;

}
