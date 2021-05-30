/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.testkit;

import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.support.AnnotationSupport;

class PioneerTestKitExtension implements ParameterResolver {

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return parameterContext.getParameter().getType().equals(ExecutionResults.class);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		Optional<TestKitTest> annotation = AnnotationSupport
				.findAnnotation(extensionContext.getTestMethod(), TestKitTest.class);
		if (annotation.isPresent()) {
			TestKitTest pioneerTest = annotation.get();
			Class<?> testClass = pioneerTest.testClass();
			String method = pioneerTest.method();
			Class<?>[] methodParameterTypes = pioneerTest.methodParameterTypes();
			if (methodParameterTypes.length > 0) {
				if (method.isEmpty()) {
					throw new PreconditionViolationException(
						"Test method name has to be defined when method parameter types are defined");
				}
				return PioneerTestKit.executeTestMethodWithParameterTypes(testClass, method, methodParameterTypes);
			} else if (!method.isEmpty()) {
				return PioneerTestKit.executeTestMethod(testClass, method);
			} else {
				return PioneerTestKit.executeTestClass(testClass);
			}
		}
		throw new ParameterResolutionException("Failed to resolve parameter because TestKitTest is missing");
	}

}
