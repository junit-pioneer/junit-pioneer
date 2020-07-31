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

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.platform.commons.util.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.stream.Stream;

/**
 * A dumb copy of {@code org.junit.jupiter.params.ParameterizedTestExtension}.
 */
public class CartesianProductTestExtension implements TestTemplateInvocationContextProvider {

	private static final Namespace NAMESPACE = Namespace.create(CartesianProductTestExtension.class);

	private static final String KEY = "CartesianProduct";

	@Override
	public boolean supportsTestTemplate(ExtensionContext context) {
		if (!context.getTestMethod().isPresent())
			return false;

		Method method = context.getRequiredTestMethod();
		if (!AnnotationUtils.isAnnotated(method, CartesianProductTest.class))
			return false;

		return PioneerAnnotationUtils.isAnyAnnotationPresent(context, CartesianProductTest.class)
				&& PioneerAnnotationUtils.isAnyAnnotationPresent(context, ArgumentsSource.class);
	}

	@Override
	public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
		return null;
	}

}
