/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.params;

import static java.lang.String.format;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.opentest4j.TestAbortedException;

class DisableIfParameterExtension implements InvocationInterceptor {

	@Override
	public void interceptTestTemplateMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		Method testMethod = extensionContext.getRequiredTestMethod();
		DisableIfParameter annotation = AnnotationSupport
				.findAnnotation(testMethod, DisableIfParameter.class)
				.orElseThrow(() -> new ExtensionConfigurationException(
					format("%s is active but no %s annotation was found. This may be a bug.",
						DisableIfParameterExtension.class.getSimpleName(), DisableIfParameter.class.getSimpleName())));
		if (annotation.contains().length == 0 && annotation.matches().length == 0)
			throw new ExtensionConfigurationException(
				format("%s requires that either `contains` or `matches` is specified, but both are empty.",
					DisableIfParameter.class.getSimpleName()));
		// Check if any argument contains any element from 'contains'
		if (invocationContext
				.getArguments()
				.stream()
				.anyMatch(arg -> Arrays.stream(annotation.contains()).anyMatch(arg.toString()::contains)))
			throw new TestAbortedException("One or more arguments contained a value from the `contains` array.");
		// Check if any argument matches any element from 'matches'
		if (invocationContext
				.getArguments()
				.stream()
				.anyMatch(arg -> Arrays.stream(annotation.matches()).anyMatch(arg.toString()::matches)))
			throw new TestAbortedException("One or more arguments matched a RegEx from the `matches` array.");
		invocation.proceed();
	}

}
