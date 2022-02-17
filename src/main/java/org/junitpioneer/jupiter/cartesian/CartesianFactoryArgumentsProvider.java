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

import static java.lang.String.format;
import static org.junit.platform.commons.support.ReflectionSupport.invokeMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.function.Try;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junitpioneer.internal.PioneerUtils;

class CartesianFactoryArgumentsProvider
		implements CartesianMethodArgumentsProvider, AnnotationConsumer<CartesianTest.Factory> {

	private String factoryMethodName;

	@Override
	public Sets provideArguments(ExtensionContext context) throws Exception {
		Method testMethod = context.getRequiredTestMethod();
		Method factory = findFactoryMethod(testMethod, factoryMethodName);
		return invokeFactoryMethod(testMethod, factory);
	}

	private Method findFactoryMethod(Method testMethod, String factoryMethodName) {
		String factoryName = extractFactoryMethodName(factoryMethodName);
		Class<?> declaringClass = findExplicitOrImplicitClass(testMethod, factoryMethodName);
		Method factory = PioneerUtils
				.findMethodCurrentOrEnclosing(declaringClass, factoryName)
				.orElseThrow(() -> new ExtensionConfigurationException("Method `Stream<? extends Arguments> "
						+ factoryName + "()` not found in " + declaringClass + " or any enclosing class."));
		String method = "Method `" + factory + "`";
		if (!Modifier.isStatic(factory.getModifiers()))
			throw new ExtensionConfigurationException(method + " must be static.");
		if (!Sets.class.isAssignableFrom(factory.getReturnType()))
			throw new ExtensionConfigurationException(
				format("%s must return a `%s` object", method, Sets.class.getName()));
		return factory;
	}

	private String extractFactoryMethodName(String factoryMethodName) {
		if (factoryMethodName.contains("("))
			factoryMethodName = factoryMethodName.substring(0, factoryMethodName.indexOf('('));
		if (factoryMethodName.contains("#"))
			return factoryMethodName.substring(factoryMethodName.indexOf('#') + 1);
		return factoryMethodName;
	}

	private Class<?> findExplicitOrImplicitClass(Method testMethod, String factoryMethodName) {
		if (!factoryMethodName.contains("#"))
			return testMethod.getDeclaringClass();

		String className = factoryMethodName.substring(0, factoryMethodName.indexOf('#'));
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

	private Sets invokeFactoryMethod(Method testMethod, Method factory) {
		Sets argumentSets = (Sets) invokeMethod(factory, null);
		long count = argumentSets.get().size();
		if (count > testMethod.getParameterCount()) {
			// If arguments count == parameters but one of the parameters should be auto-injected by JUnit
			// JUnit will throw a ParameterResolutionException for competing resolvers before we could get to this line
			throw new ParameterResolutionException(format(
				"Method `%s` must register values for each parameter exactly once. Expected [%d] parameter sets, but got [%d].",
				factory, testMethod.getParameterCount(), count));
		}
		return argumentSets;
	}

	@Override
	public void accept(CartesianTest.Factory factory) {
		this.factoryMethodName = factory.value();
	}

}
