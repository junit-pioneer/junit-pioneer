/*
 * Copyright 2016-2022 the original author or authors.
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
 * Provides arguments for all parameters of a {@link CartesianTest} method.
 * <p>
 * For more information, see
 * <a href="https://junit-pioneer.org/docs/cartesian-product/" target="_top">the Cartesian product documentation</a>.
 * </p>
 */
public interface CartesianMethodArgumentsProvider extends CartesianArgumentsProvider {

	/**
	 * Provides an {@link ArgumentSets} object, containing the arguments for each parameter in order,
	 * to be used for the {@link CartesianTest}.
	 *
	 * @param context the current extension context
	 * @return a {@link ArgumentSets} object
	 */
	ArgumentSets provideArguments(ExtensionContext context) throws Exception;

}
