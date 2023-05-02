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

import static java.lang.String.format;
import static org.junit.platform.commons.support.ReflectionSupport.invokeMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.function.Try;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junitpioneer.internal.PioneerUtils;

class CartesianFactoryArgumentsProvider
		implements CartesianMethodArgumentsProvider, AnnotationConsumer<CartesianTest.MethodFactory> {

	private String methodFactoryName;

	@Override
	public ArgumentSets provideArguments(ExtensionContext context) throws Exception {
		Method testMethod = context.getRequiredTestMethod();
		Object testInstance = context.getTestInstance().orElse(null);
		TestInstance.Lifecycle lifecycle = context.getTestInstanceLifecycle().orElse(null);
		Method factory = findMethodFactory(testMethod, methodFactoryName, testInstance, lifecycle);
		return invokeMethodFactory(testMethod, factory, testInstance);
	}

	private static Method findMethodFactory(Method testMethod, String methodFactoryName, Object testInstance,
			TestInstance.Lifecycle lifecycle) {
		String factoryName = extractMethodFactoryName(methodFactoryName);
		Class<?> declaringClass = findExplicitOrImplicitClass(testMethod, methodFactoryName);
		Method factory = PioneerUtils
				.findMethodCurrentOrEnclosing(declaringClass, factoryName)
				.orElseThrow(() -> new ExtensionConfigurationException("Method `Stream<? extends Arguments> "
						+ factoryName + "()` not found in " + declaringClass + " or any enclosing class."));
		String method = "Method `" + factory + "`";
		if (factoryMustBeStatic(factory, testInstance, lifecycle) && !Modifier.isStatic(factory.getModifiers()))
			throw new ExtensionConfigurationException(method + " must be static.");
		if (!ArgumentSets.class.isAssignableFrom(factory.getReturnType()))
			throw new ExtensionConfigurationException(
				format("%s must return a `%s` object", method, ArgumentSets.class.getName()));
		return factory;
	}

	private static String extractMethodFactoryName(String methodFactoryName) {
		if (methodFactoryName.contains("("))
			methodFactoryName = methodFactoryName.substring(0, methodFactoryName.indexOf('('));
		if (methodFactoryName.contains("#"))
			return methodFactoryName.substring(methodFactoryName.indexOf('#') + 1);
		return methodFactoryName;
	}

	private static Class<?> findExplicitOrImplicitClass(Method testMethod, String methodFactoryName) {
		if (!methodFactoryName.contains("#"))
			return testMethod.getDeclaringClass();

		String className = methodFactoryName.substring(0, methodFactoryName.indexOf('#'));
		Try<Class<?>> tryToLoadClass = ReflectionSupport.tryToLoadClass(className);
		// step (outwards) through all enclosing classes, trying to load the factory class by appending
		// its name to the enclosing class' name (if a previous load didn't already succeed
		Class<?> methodClass = testMethod.getDeclaringClass();
		while (methodClass != null) {
			String enclosingName = methodClass.getName();
			tryToLoadClass = tryToLoadClass
					.orElse(() -> ReflectionSupport.tryToLoadClass(enclosingName + "$" + className));
			methodClass = methodClass.getEnclosingClass();
		}
		return tryToLoadClass
				.getOrThrow(ex -> new ExtensionConfigurationException(
					format("Class %s not found, referenced in method %s", className, testMethod.getName()), ex));
	}

	private static boolean factoryMustBeStatic(Method factory, Object testInstance, TestInstance.Lifecycle lifecycle) {
		return testInstance == null || !factory.getDeclaringClass().isInstance(testInstance)
				|| lifecycle != TestInstance.Lifecycle.PER_CLASS;
	}

	private ArgumentSets invokeMethodFactory(Method testMethod, Method factory, Object testInstance) {
		Object target = factory.getDeclaringClass().isInstance(testInstance) ? testInstance : null;
		ArgumentSets argumentSets = (ArgumentSets) invokeMethod(factory, target);
		long count = argumentSets.getArguments().size();
		if (count > testMethod.getParameterCount()) {
			// If arguments count == parameters but one of the parameters should be auto-injected by JUnit.
			// JUnit will throw a ParameterResolutionException for competing resolvers before we could get to this line.
			throw new ParameterResolutionException(format(
				"Method `%s` must register values for each parameter exactly once. Expected [%d] parameter sets, but got [%d].",
				factory, testMethod.getParameterCount(), count));
		}
		return argumentSets;
	}

	@Override
	public void accept(CartesianTest.MethodFactory factory) {
		this.methodFactoryName = factory.value();
	}

}
