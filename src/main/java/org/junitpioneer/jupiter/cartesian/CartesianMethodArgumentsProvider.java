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

import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * If you are implementing an {@link org.junit.jupiter.params.provider.ArgumentsProvider ArgumentsProvider}
 * for {@link CartesianTest}, it has to implement this interface <b>as well</b> to provide arguments simultaneously
 * for all parameters in a test method. For more information, see
 * <a href="https://junit-pioneer.org/docs/cartesian-product/" target="_top">the Cartesian product documentation</a>.
 *
 * @see org.junit.jupiter.params.provider.ArgumentsProvider
 */
public interface CartesianMethodArgumentsProvider extends CartesianArgumentsProvider {

	/**
	 * Provides a {@link ArgumentSets} object, containing the arguments for each parameter in order,
	 * to be used for the {@code @CartesianTest}.
	 * For more information, see
	 * <a href="https://junit-pioneer.org/docs/cartesian-product/" target="_top">the Cartesian product documentation</a>.
	 *
	 * @param context the current extension context; never {@code null}
	 * @return a {@link ArgumentSets} object; never {@code null}
	 */
	ArgumentSets provideArguments(ExtensionContext context) throws Exception;

}
