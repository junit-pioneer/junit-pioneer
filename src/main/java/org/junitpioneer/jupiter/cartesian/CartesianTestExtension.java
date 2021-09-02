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
import static java.util.stream.Collectors.toList;
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
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junitpioneer.internal.PioneerAnnotationUtils;

class CartesianTestExtension implements TestTemplateInvocationContextProvider {

	@Override
	public boolean supportsTestTemplate(ExtensionContext context) {
		return findAnnotation(context.getTestMethod(), CartesianTest.class).isPresent();
	}

	@Override
	public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
		List<List<?>> sets = computeSets(context);
		CartesianTestNameFormatter formatter = createNameFormatter(context);
		return cartesianProduct(sets).stream().map(params -> new CartesianTestInvocationContext(params, formatter));
	}

	private CartesianTestNameFormatter createNameFormatter(ExtensionContext context) {
		CartesianTest annotation = findAnnotation(context.getRequiredTestMethod(), CartesianTest.class)
				.orElseThrow(() -> new ExtensionConfigurationException("@CartesianTest not found."));
		String pattern = annotation.name();
		if (pattern.isEmpty())
			throw new ExtensionConfigurationException("CartesianTest can not have a non-empty display name.");
		String displayName = context.getDisplayName();
		return new CartesianTestNameFormatter(pattern, displayName);
	}

	private List<List<?>> computeSets(ExtensionContext context) {
		Method testMethod = context.getRequiredTestMethod();
		List<? extends Annotation> methodArgumentsSources = PioneerAnnotationUtils
				.findAnnotatedAnnotations(testMethod, ArgumentsSource.class);
		List<? extends Annotation> parameterArgumentsSources = PioneerAnnotationUtils
				.findParameterArgumentsSources(testMethod);
		ensureNoInputConflicts(methodArgumentsSources, parameterArgumentsSources, testMethod);
		return getSetsFromArgumentsSources(parameterArgumentsSources, context);
	}

	private static void ensureNoInputConflicts(List<?> methodSources, List<?> parameterSources, Method testMethod) {
		if (methodSources.isEmpty() && parameterSources.isEmpty() && testMethod.getParameters().length > 0)
			throw new ExtensionConfigurationException("No arguments sources were found for @CartesianTest");
		if (!methodSources.isEmpty() && !parameterSources.isEmpty())
			throw new ExtensionConfigurationException(
				"Providing both method-level and parameter-level argument sources for @CartesianTest is not supported.");
	}

	private List<List<?>> getSetsFromArgumentsSources(List<? extends Annotation> argumentsSources,
			ExtensionContext context) {
		List<List<?>> sets = new ArrayList<>();
		List<Parameter> parameters = Arrays.asList(context.getRequiredTestMethod().getParameters());
		for (int i = 0; i < Math.min(parameters.size(), argumentsSources.size()); i++) {
			sets.add(getSetFromAnnotation(context, argumentsSources.get(i), parameters.get(i)));
		}
		return sets;
	}

	private List<Object> getSetFromAnnotation(ExtensionContext context, Annotation source, Parameter parameter) {
		try {
			CartesianArgumentsProvider provider = initializeArgumentsProvider(source);
			return provideArguments(context, parameter, provider);
		}
		catch (Exception ex) {
			throw new ExtensionConfigurationException("Could not provide arguments because of exception.", ex);
		}
	}

	private CartesianArgumentsProvider initializeArgumentsProvider(Annotation source) {
		ArgumentsSource providerAnnotation = AnnotationSupport
				.findAnnotation(source.annotationType(), ArgumentsSource.class)
				// never happens, we already know these annotations are annotated with @ArgumentsSource
				.orElseThrow(() -> new PreconditionViolationException(format(
					"%s was not annotated with @ArgumentsSource but should have been.", source.annotationType())));
		ArgumentsProvider provider = ReflectionSupport.newInstance(providerAnnotation.value());
		if (provider instanceof CartesianArgumentsProvider)
			return (CartesianArgumentsProvider) provider;
		else
			throw new PreconditionViolationException(
				format("%s does not implement the CartesianArgumentsProvider interface.", provider.getClass()));
	}

	private List<Object> provideArguments(ExtensionContext context, Parameter source,
			CartesianArgumentsProvider provider) throws Exception {
		provider.accept(source);
		return provider
				.provideArguments(context)
				.map(Arguments::get)
				.flatMap(Arrays::stream)
				.distinct()
				// We like to keep arguments in the order in which they were listed
				// in the annotation. Could use a set with defined iteration, but
				// this is more explicit.
				.collect(toList());
	}

}
