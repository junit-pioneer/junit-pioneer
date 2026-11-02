/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.resource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.junit.jupiter.api.extension.DynamicTestInvocationContext;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

class ResourceExtension implements ParameterResolver, InvocationInterceptor {

	private static final ExtensionContext.Namespace NAMESPACE = //
		ExtensionContext.Namespace.create(ResourceExtension.class);

	private static final SharedResourceCoordinator SHARED_RESOURCE_COORDINATOR = //
		new SharedResourceCoordinator(NAMESPACE);

	private static final ResourceResolver RESOURCE_RESOLVER = //
		new ResourceResolver(NAMESPACE, SHARED_RESOURCE_COORDINATOR);

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return RESOURCE_RESOLVER.supportsParameter(parameterContext, extensionContext);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return RESOURCE_RESOLVER.resolveParameter(parameterContext, extensionContext);
	}

	@Override
	public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
			ExtensionContext extensionContext) throws Throwable {
		SHARED_RESOURCE_COORDINATOR.runSequentially(invocation, invocationContext.getExecutable(), extensionContext);
	}

	@Override
	public <T> T interceptTestFactoryMethod(Invocation<T> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		return SHARED_RESOURCE_COORDINATOR
				.runSequentially(invocation, invocationContext.getExecutable(), extensionContext);
	}

	@Override
	public void interceptDynamicTest(Invocation<Void> invocation, DynamicTestInvocationContext invocationContext,
			ExtensionContext extensionContext) throws Throwable {
		SHARED_RESOURCE_COORDINATOR.runSequentially(invocation, testFactoryMethod(extensionContext), extensionContext);
	}

	@Override
	public void interceptTestTemplateMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		SHARED_RESOURCE_COORDINATOR.runSequentially(invocation, invocationContext.getExecutable(), extensionContext);
	}

	@Override
	public <T> T interceptTestClassConstructor(Invocation<T> invocation,
			ReflectiveInvocationContext<Constructor<T>> invocationContext, ExtensionContext extensionContext)
			throws Throwable {
		return SHARED_RESOURCE_COORDINATOR
				.runSequentially(invocation, invocationContext.getExecutable(), extensionContext);
	}

	@Override
	public void interceptBeforeAllMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		SHARED_RESOURCE_COORDINATOR.runSequentially(invocation, invocationContext.getExecutable(), extensionContext);
	}

	@Override
	public void interceptAfterAllMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		SHARED_RESOURCE_COORDINATOR.runSequentially(invocation, invocationContext.getExecutable(), extensionContext);
	}

	@Override
	public void interceptBeforeEachMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		SHARED_RESOURCE_COORDINATOR.runSequentially(invocation, invocationContext.getExecutable(), extensionContext);
	}

	@Override
	public void interceptAfterEachMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		SHARED_RESOURCE_COORDINATOR.runSequentially(invocation, invocationContext.getExecutable(), extensionContext);
	}

	private Method testFactoryMethod(ExtensionContext extensionContext) {
		return extensionContext
				.getParent()
				.orElseThrow(() -> new IllegalStateException(
					"The parent extension context of a DynamicTest was not a @TestFactory-annotated test method"))
				.getRequiredTestMethod();
	}

}
