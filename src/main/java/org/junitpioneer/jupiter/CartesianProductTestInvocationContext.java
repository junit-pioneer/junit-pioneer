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

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

public class CartesianProductTestInvocationContext implements TestTemplateInvocationContext {

	private final List<?> parameters;

	CartesianProductTestInvocationContext(List<?> parameters) {
		this.parameters = parameters;
	}

	@Override
	public String getDisplayName(int invocationIndex) {
		return "[" + invocationIndex + "] "
		// removes the [] characters
				+ parameters.toString().substring(1, parameters.toString().length() - 1);
	}

	@Override
	public List<Extension> getAdditionalExtensions() {
		return Collections.singletonList(new CartesianProductResolver(parameters));
	}

}
