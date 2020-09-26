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

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
import static org.junit.platform.commons.support.AnnotationSupport.findRepeatableAnnotations;
import static org.junit.platform.commons.support.ReflectionSupport.invokeMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

class CartesianProductTestExtension implements TestTemplateInvocationContextProvider {

	@Override
	public boolean supportsTestTemplate(ExtensionContext context) {
		return findAnnotation(context.getTestMethod(), CartesianProductTest.class).isPresent();
	}

	@Override
	public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
		List<List<?>> sets = computeSets(context);
		return cartesianProduct(sets).stream().map(CartesianProductTestInvocationContext::new);
	}

	private List<List<?>> computeSets(ExtensionContext context) {
		Method testMethod = context.getRequiredTestMethod();
		CartesianProductTest annotation = findAnnotation(testMethod, CartesianProductTest.class)
				.orElseThrow(() -> new ExtensionConfigurationException("@CartesianProductTest not found"));
		List<CartesianValueSource> valueSources = findRepeatableAnnotations(testMethod, CartesianValueSource.class);
		ensureNoInputConflicts(annotation, valueSources);
		// Compute A ⨯ A ⨯ ... ⨯ A from single source "set"
		if (annotation.value().length > 0) {
			return getSetsFromValue(testMethod, annotation);
		}
		// Try finding the @CartesianValueSource annotation
		if (!valueSources.isEmpty()) {
			return getSetsFromRepeatableAnnotation(valueSources);
		}
		// Try the sets static factory method
		return getSetsFromStaticFactory(testMethod, annotation.factory());
	}

	private static void ensureNoInputConflicts(CartesianProductTest annotation,
			List<CartesianValueSource> valueSources) {
		boolean hasValue = annotation.value().length != 0;
		boolean hasFactory = !annotation.factory().isEmpty();
		boolean hasValueSources = !valueSources.isEmpty();
		if (hasValue && hasFactory || hasValue && hasValueSources || hasFactory && hasValueSources) {
			throw new ExtensionConfigurationException(
				"CartesianProductTest can only take exactly one type of arguments source.");
		}
	}

	private List<List<?>> getSetsFromValue(Method testMethod, CartesianProductTest annotation) {
		List<List<?>> sets = new ArrayList<>();
		List<String> strings = Arrays.stream(annotation.value()).distinct().collect(toList());
		for (int i = 0; i < testMethod.getParameterTypes().length; i++) {
			sets.add(strings);
		}
		return sets;
	}

	private List<List<?>> getSetsFromRepeatableAnnotation(List<CartesianValueSource> valueSources) {
		List<List<?>> sets = new ArrayList<>();
		for (CartesianValueSource source : valueSources) {
			CartesianValueArgumentsProvider provider = new CartesianValueArgumentsProvider();
			provider.accept(source);
			List<Object> collect = provider.provideArguments().distinct().collect(toList());
			sets.add(collect);
		}
		return sets;
	}

	private List<List<?>> getSetsFromStaticFactory(Method testMethod, String explicitFactoryName) {
		if (explicitFactoryName.isEmpty())
			return invokeSetsFactory(testMethod, testMethod.getName()).getSets();
		else
			return invokeSetsFactory(testMethod, explicitFactoryName).getSets();
	}

	private CartesianProductTest.Sets invokeSetsFactory(Method testMethod, String factoryMethodName) {
		Class<?> declaringClass = testMethod.getDeclaringClass();
		Method factory = PioneerUtils
				.findMethodCurrentOrEnclosing(declaringClass, factoryMethodName)
				.orElseThrow(() -> new ExtensionConfigurationException("Method `CartesianProductTest.Sets "
						+ factoryMethodName + "()` not found in " + declaringClass + "or any enclosing class"));
		String method = "Method `" + factory + "`";
		if (!Modifier.isStatic(factory.getModifiers())) {
			throw new ExtensionConfigurationException(method + " must be static");
		}
		if (!CartesianProductTest.Sets.class.isAssignableFrom(factory.getReturnType())) {
			throw new ExtensionConfigurationException(method + " must return `CartesianProductTest.Sets`");
		}
		CartesianProductTest.Sets sets = (CartesianProductTest.Sets) invokeMethod(factory, null);
		if (sets.getSets().size() > testMethod.getParameterCount()) {
			throw new ParameterResolutionException(format(
				"%s must register values for each parameter exactly once. Expected [%d] parameter sets, but got [%d]",
				method, testMethod.getParameterCount(), sets.getSets().size()));
		}
		return sets;
	}

	private static List<List<?>> cartesianProduct(List<List<?>> lists) {
		List<List<?>> resultLists = new ArrayList<>();
		if (lists.isEmpty()) {
			resultLists.add(Collections.emptyList());
			return resultLists;
		}
		List<?> firstList = lists.get(0);
		// Note the recursion here
		List<List<?>> remainingLists = cartesianProduct(lists.subList(1, lists.size()));
		for (Object item : firstList) {
			for (List<?> remainingList : remainingLists) {
				ArrayList<Object> resultList = new ArrayList<>();
				resultList.add(item);
				resultList.addAll(remainingList);
				resultLists.add(resultList);
			}
		}
		return resultLists;
	}

}
