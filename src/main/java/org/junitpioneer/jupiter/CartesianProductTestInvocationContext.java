/*
 * Copyright 2016-2021 the original author or authors.
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
import org.junitpioneer.internal.TestNameFormatter;

/**
 * @deprecated scheduled to be removed in 2.0
 */
@Deprecated
class CartesianProductTestInvocationContext implements TestTemplateInvocationContext {

	private final List<?> parameters;
	private final TestNameFormatter formatter;

	CartesianProductTestInvocationContext(List<?> parameters, TestNameFormatter formatter) {
		this.parameters = parameters;
		this.formatter = formatter;
	}

	@Override
	public String getDisplayName(int invocationIndex) {
		return formatter.format(invocationIndex, parameters.toArray());
	}

	@Override
	public List<Extension> getAdditionalExtensions() {
		return Collections.singletonList(new CartesianProductResolver(parameters));
	}

}
