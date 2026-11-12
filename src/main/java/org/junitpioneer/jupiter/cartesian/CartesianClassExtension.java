/*
 * Copyright 2016-2026 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.cartesian;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_METHOD;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
import static org.junitpioneer.internal.PioneerUtils.cartesianProduct;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.ClassTemplateInvocationContext;
import org.junit.jupiter.api.extension.ClassTemplateInvocationContextProvider;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junitpioneer.internal.TestNameFormatter;

class CartesianClassExtension implements ClassTemplateInvocationContextProvider, ParameterResolver {

	private static void validateLifecycle(ExtensionContext context) {
		if (context.getTestInstanceLifecycle().orElse(PER_METHOD) == PER_CLASS)
			throw new ExtensionConfigurationException(
				"@CartesianClass constructor injection requires @TestInstance(Lifecycle.PER_METHOD).");
	}

	// PER_CLASS construction happens before invocation contexts are available. As in
	// Jupiter's ParameterizedClassExtension, this resolver provides an early diagnostic
	// but leaves argument resolution to each invocation's constructor-only resolver.
	@Override
	public boolean supportsParameter(ParameterContext parameter, ExtensionContext context) {
		if (parameter.getDeclaringExecutable() instanceof Constructor<?>
				&& parameter.getDeclaringExecutable().getDeclaringClass().equals(context.getRequiredTestClass())
				&& findAnnotation(context.getRequiredTestClass(), CartesianClass.class).isPresent())
			validateLifecycle(context);
		return false;
	}

	@Override
	public Object resolveParameter(ParameterContext parameter, ExtensionContext context) {
		throw new IllegalStateException("Constructor arguments are resolved by the class invocation.");
	}

	@Override
	public boolean supportsClassTemplate(ExtensionContext context) {
		return findAnnotation(context.getRequiredTestClass(), CartesianClass.class).isPresent();
	}

	@Override
	public Stream<ClassTemplateInvocationContext> provideClassTemplateInvocationContexts(ExtensionContext context) {
		validateLifecycle(context);
		Class<?> testClass = context.getRequiredTestClass();
		if (testClass.isMemberClass() && !Modifier.isStatic(testClass.getModifiers()))
			throw new ExtensionConfigurationException("@CartesianClass requires a top-level or static nested class.");
		Constructor<?>[] constructors = testClass.getDeclaredConstructors();
		if (constructors.length != 1)
			throw new ExtensionConfigurationException("@CartesianClass requires exactly one constructor.");
		Constructor<?> constructor = constructors[0];
		CartesianClass annotation = findAnnotation(testClass, CartesianClass.class).orElseThrow();
		if (annotation.name().isBlank())
			throw new ExtensionConfigurationException("CartesianClass can not have a blank display name.");
		TestNameFormatter formatter = new TestNameFormatter(annotation.name(), context.getDisplayName(),
			CartesianClass.class);
		List<List<?>> sets = new ArrayList<>();
		for (Parameter parameter : constructor.getParameters()) {
			List<Annotation> sources = new ArrayList<>();
			AnnotationSupport.findAnnotation(parameter, CartesianArgumentsSource.class).ifPresent(sources::add);
			sources.addAll(AnnotationSupport.findRepeatableAnnotations(parameter, ArgumentsSource.class));
			if (sources.size() != 1)
				throw new ExtensionConfigurationException(
					"Each @CartesianClass constructor parameter requires exactly one arguments source: "
							+ parameter.getName());
			sets.add(CartesianParameterArguments.getSetFromAnnotation(context, sources.get(0), parameter));
		}
		return cartesianProduct(sets).stream().map(parameters -> new ClassTemplateInvocationContext() {

			@Override
			public String getDisplayName(int invocationIndex) {
				return formatter.format(invocationIndex, parameters.toArray());
			}

			@Override
			public List<Extension> getAdditionalExtensions() {
				return List.of(new ConstructorArgumentsResolver(constructor, parameters));
			}

		});
	}

	private static class ConstructorArgumentsResolver implements ParameterResolver {

		private final Constructor<?> constructor;
		private final List<?> arguments;

		ConstructorArgumentsResolver(Constructor<?> constructor, List<?> arguments) {
			this.constructor = constructor;
			this.arguments = arguments;
		}

		@Override
		public boolean supportsParameter(ParameterContext parameter, ExtensionContext context) {
			return parameter.getDeclaringExecutable().equals(constructor);
		}

		@Override
		public Object resolveParameter(ParameterContext parameter, ExtensionContext context) {
			Object argument = arguments.get(parameter.getIndex());
			return argument instanceof Named<?> ? ((Named<?>) argument).getPayload() : argument;
		}

	}

}
