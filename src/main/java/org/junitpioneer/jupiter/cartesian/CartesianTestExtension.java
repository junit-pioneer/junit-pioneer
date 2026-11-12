/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.cartesian;

import static java.lang.String.format;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
import static org.junitpioneer.internal.PioneerUtils.cartesianProduct;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.jupiter.params.support.AnnotationConsumerInitializer;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junitpioneer.internal.PioneerAnnotationUtils;
import org.junitpioneer.internal.TestNameFormatter;

class CartesianTestExtension implements TestTemplateInvocationContextProvider {

	@Override
	public boolean supportsTestTemplate(ExtensionContext context) {
		return findAnnotation(context.getTestMethod(), CartesianTest.class).isPresent();
	}

	@Override
	public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
		List<List<?>> sets = computeSets(context);
		TestNameFormatter formatter = createNameFormatter(context);
		return cartesianProduct(sets).stream().map(params -> new CartesianTestInvocationContext(params, formatter));
	}

	private TestNameFormatter createNameFormatter(ExtensionContext context) {
		CartesianTest annotation = findAnnotation(context.getRequiredTestMethod(), CartesianTest.class)
				.orElseThrow(() -> new ExtensionConfigurationException("@CartesianTest not found."));
		String pattern = annotation.name();
		if (pattern.isEmpty())
			throw new ExtensionConfigurationException("CartesianTest can not have an empty display name.");
		String displayName = context.getDisplayName();
		return new TestNameFormatter(pattern, displayName, CartesianTest.class);
	}

	private List<List<?>> computeSets(ExtensionContext context) {
		Method testMethod = context.getRequiredTestMethod();
		List<? extends Annotation> methodArgumentsSources = PioneerAnnotationUtils
				.findMethodArgumentsSources(testMethod);
		List<? extends Annotation> parameterArgumentsSources = PioneerAnnotationUtils
				.findParameterArgumentsSources(testMethod);
		ensureNoInputConflicts(methodArgumentsSources, parameterArgumentsSources, testMethod);
		if (!methodArgumentsSources.isEmpty())
			return getSetsFromMethodArgumentsSource(methodArgumentsSources.get(0), context);
		return getSetsFromArgumentsSources(parameterArgumentsSources, context);
	}

	private static void ensureNoInputConflicts(List<?> methodSources, List<?> parameterSources, Method testMethod) {
		if (methodSources.isEmpty() && parameterSources.isEmpty() && testMethod.getParameters().length > 0)
			throw new ExtensionConfigurationException("No arguments sources were found for @CartesianTest");
		if (!methodSources.isEmpty() && !parameterSources.isEmpty())
			throw new ExtensionConfigurationException(
				"Providing both method-level and parameter-level argument sources for @CartesianTest is not supported.");
		if (methodSources.size() > 1)
			throw new ExtensionConfigurationException(
				"Only one method-level arguments source can be used with @CartesianTest");
	}

	private List<List<?>> getSetsFromMethodArgumentsSource(Annotation argumentsSource, ExtensionContext context) {
		try {
			CartesianMethodArgumentsProvider provider = initializeMethodArgumentsProvider(argumentsSource,
				context.getRequiredTestMethod());
			return provider.provideArguments(context).getArguments();
		}
		catch (Exception ex) {
			throw new ExtensionConfigurationException("Could not provide arguments because of exception.", ex);
		}
	}

	private List<List<?>> getSetsFromArgumentsSources(List<? extends Annotation> argumentsSources,
			ExtensionContext context) {
		List<List<?>> sets = new ArrayList<>();
		List<Parameter> parameters = Arrays.asList(context.getRequiredTestMethod().getParameters());
		for (int i = 0; i < Math.min(parameters.size(), argumentsSources.size()); i++) {
			sets
					.add(CartesianParameterArguments
							.getSetFromAnnotation(context, argumentsSources.get(i), parameters.get(i)));
		}
		return sets;
	}

	private CartesianMethodArgumentsProvider initializeMethodArgumentsProvider(Annotation source, Method method) {
		CartesianArgumentsSource providerAnnotation = AnnotationSupport
				.findAnnotation(method, CartesianArgumentsSource.class)
				// never happens, we already know these annotations are annotated with @ArgumentsSource
				.orElseThrow(() -> new IllegalStateException(format(
					"%s was not annotated with @CartesianArgumentsSource or @ArgumentsSource but should have been.",
					source.annotationType())));
		CartesianArgumentsProvider provider = ReflectionSupport.newInstance(providerAnnotation.value());
		if (!(provider instanceof CartesianMethodArgumentsProvider))
			throw new PreconditionViolationException(format("%s does not implement %s interface.", provider.getClass(),
				CartesianMethodArgumentsProvider.class.getSimpleName()));
		return AnnotationConsumerInitializer.initialize(method, (CartesianMethodArgumentsProvider) provider);
	}

}
