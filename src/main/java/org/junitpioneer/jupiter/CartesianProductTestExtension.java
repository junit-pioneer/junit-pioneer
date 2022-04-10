/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
import static org.junit.platform.commons.support.ReflectionSupport.invokeMethod;
import static org.junitpioneer.internal.PioneerUtils.cartesianProduct;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junitpioneer.internal.PioneerAnnotationUtils;
import org.junitpioneer.internal.PioneerUtils;
import org.junitpioneer.internal.TestNameFormatter;

/**
 * @deprecated Replaced by `org.junitpioneer.jupiter.cartesian.CartesianTestExtension`.
 * Scheduled to be removed in 2.0
 */
@Deprecated
class CartesianProductTestExtension implements TestTemplateInvocationContextProvider {

	@Override
	public boolean supportsTestTemplate(ExtensionContext context) {
		return findAnnotation(context.getTestMethod(), CartesianProductTest.class).isPresent();
	}

	@Override
	public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
		List<List<?>> sets = computeSets(context);
		TestNameFormatter formatter = createNameFormatter(context);
		return cartesianProduct(sets)
				.stream()
				.map(params -> new CartesianProductTestInvocationContext(params, formatter));
	}

	private TestNameFormatter createNameFormatter(ExtensionContext context) {
		CartesianProductTest annotation = findAnnotation(context.getRequiredTestMethod(), CartesianProductTest.class)
				.orElseThrow(() -> new ExtensionConfigurationException("@CartesianProductTest not found."));
		String pattern = annotation.name();
		if (pattern.isEmpty())
			throw new ExtensionConfigurationException("CartesianProductTest can not have a non-empty display name.");
		String displayName = context.getDisplayName();
		return new TestNameFormatter(pattern, displayName, CartesianProductTest.class);
	}

	private List<List<?>> computeSets(ExtensionContext context) {
		Method testMethod = context.getRequiredTestMethod();
		CartesianProductTest annotation = findAnnotation(testMethod, CartesianProductTest.class)
				.orElseThrow(() -> new ExtensionConfigurationException("@CartesianProductTest not found."));
		List<? extends Annotation> argumentsSources = PioneerAnnotationUtils
				.findAnnotatedAnnotations(testMethod, ArgumentsSource.class);
		ensureNoInputConflicts(annotation, argumentsSources);
		// Compute A ⨯ A ⨯ ... ⨯ A from single source "set"
		if (annotation.value().length > 0)
			return getSetsFromValue(testMethod, annotation);
		// Try getting sets from the @ArgumentsSource annotations
		if (!argumentsSources.isEmpty())
			return getSetsFromArgumentsSources(argumentsSources, context);
		// Try the sets static factory method
		return getSetsFromStaticFactory(testMethod, annotation.factory());
	}

	private static void ensureNoInputConflicts(CartesianProductTest annotation,
			List<? extends Annotation> valueSources) {
		boolean hasValue = annotation.value().length != 0;
		boolean hasFactory = !annotation.factory().isEmpty();
		boolean hasValueSources = !valueSources.isEmpty();
		if (hasValue && hasFactory || hasValue && hasValueSources || hasFactory && hasValueSources)
			throw new ExtensionConfigurationException(
				"CartesianProductTest can only take exactly one type of arguments source.");
	}

	private List<List<?>> getSetsFromValue(Method testMethod, CartesianProductTest annotation) {
		List<List<?>> sets = new ArrayList<>();
		List<String> strings = Arrays.stream(annotation.value()).distinct().collect(toList());
		for (int i = 0; i < testMethod.getParameterTypes().length; i++)
			sets.add(strings);
		return sets;
	}

	private List<List<?>> getSetsFromArgumentsSources(List<? extends Annotation> argumentsSources,
			ExtensionContext context) {
		List<List<?>> sets = new ArrayList<>();
		for (Annotation source : argumentsSources)
			sets.add(getSetFromAnnotation(context, source));
		return sets;
	}

	private List<Object> getSetFromAnnotation(ExtensionContext context, Annotation source) {
		try {
			ArgumentsProvider provider = initializeArgumentsProvider(source);
			return provideArguments(context, source, provider);
		}
		catch (Exception ex) {
			throw new ExtensionConfigurationException("Could not provide arguments because of exception.", ex);
		}
	}

	private ArgumentsProvider initializeArgumentsProvider(Annotation source) {
		ArgumentsSource providerAnnotation = AnnotationSupport
				.findAnnotation(source.annotationType(), ArgumentsSource.class)
				// never happens, we already know these annotations are annotated with @ArgumentsSource
				.orElseThrow(() -> new PreconditionViolationException(format(
					"%s was not annotated with @ArgumentsSource but should have been.", source.annotationType())));
		return ReflectionSupport.newInstance(providerAnnotation.value());
	}

	@SuppressWarnings("unchecked")
	private List<Object> provideArguments(ExtensionContext context, Annotation source, ArgumentsProvider provider)
			throws Exception {
		if (provider instanceof CartesianAnnotationConsumer) {
			((CartesianAnnotationConsumer<Annotation>) provider).accept(source);
			return provider
					.provideArguments(context)
					.map(Arguments::get)
					.flatMap(Arrays::stream)
					.distinct()
					.collect(toList());
		} else {
			throw new PreconditionViolationException(
				format("%s does not implement the CartesianAnnotationConsumer<T> interface.", provider.getClass()));
		}
	}

	private List<List<?>> getSetsFromStaticFactory(Method testMethod, String explicitFactoryName) {
		if (explicitFactoryName.isEmpty())
			return invokeSetsFactory(testMethod, testMethod.getName()).getSets();
		else
			return invokeSetsFactory(testMethod, explicitFactoryName).getSets();
	}

	private CartesianProductTest.Sets invokeSetsFactory(Method testMethod, String factoryMethodName) {
		Method factory = findSetsFactory(testMethod, factoryMethodName);
		return invokeSetsFactory(testMethod, factory);
	}

	private Method findSetsFactory(Method testMethod, String factoryMethodName) {
		String factoryName = getFactoryMethodName(factoryMethodName);
		Class<?> declaringClass = getExplicitOrImplicitClass(testMethod, factoryMethodName);
		Method factory = PioneerUtils
				.findMethodCurrentOrEnclosing(declaringClass, factoryName)
				.orElseThrow(() -> new ExtensionConfigurationException("Method `CartesianProductTest.Sets "
						+ factoryName + "()` not found in " + declaringClass + " or any enclosing class."));
		String method = "Method `" + factory + "`";
		if (!Modifier.isStatic(factory.getModifiers()))
			throw new ExtensionConfigurationException(method + " must be static.");
		if (!CartesianProductTest.Sets.class.isAssignableFrom(factory.getReturnType()))
			throw new ExtensionConfigurationException(method + " must return `CartesianProductTest.Sets`.");
		return factory;
	}

	private String getFactoryMethodName(String factoryMethodName) {
		if (factoryMethodName.contains("("))
			factoryMethodName = factoryMethodName.substring(0, factoryMethodName.indexOf('('));
		if (factoryMethodName.contains("#"))
			return factoryMethodName.substring(factoryMethodName.indexOf('#') + 1);
		return factoryMethodName;
	}

	private Class<?> getExplicitOrImplicitClass(Method testMethod, String factoryMethodName) {
		if (factoryMethodName.contains("#")) {
			String className = factoryMethodName.substring(0, factoryMethodName.indexOf('#'));
			return ReflectionSupport
					.tryToLoadClass(className)
					.getOrThrow(ex -> new ExtensionConfigurationException(
						format("Class %s not found, referenced in method %s", className, testMethod.getName()), ex));

		}
		return testMethod.getDeclaringClass();
	}

	private CartesianProductTest.Sets invokeSetsFactory(Method testMethod, Method factory) {
		CartesianProductTest.Sets sets = (CartesianProductTest.Sets) invokeMethod(factory, null);
		if (sets.getSets().size() > testMethod.getParameterCount()) {
			// If sets == parameters but one of the parameters should be auto-injected by JUnit
			// JUnit will throw a ParameterResolutionException for competing resolvers before we could get to this line
			throw new ParameterResolutionException(format(
				"Method `%s` must register values for each parameter exactly once. Expected [%d] parameter sets, but got [%d].",
				factory, testMethod.getParameterCount(), sets.getSets().size()));
		}
		return sets;
	}

}
