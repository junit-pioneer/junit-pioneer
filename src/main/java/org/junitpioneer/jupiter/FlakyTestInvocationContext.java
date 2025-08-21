/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import java.util.List;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junitpioneer.internal.TestNameFormatter;

class FlakyTestInvocationContext implements TestTemplateInvocationContext {

	private final TestNameFormatter formatter;

	FlakyTestInvocationContext(TestNameFormatter formatter) {
		this.formatter = formatter;
	}

	@Override
	public String getDisplayName(int invocationIndex) {
		return formatter.format(invocationIndex);
	}

	@Override
	public List<Extension> getAdditionalExtensions() {
		return List.of();
	}

}
