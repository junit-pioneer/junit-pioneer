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

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
import static org.junit.platform.commons.support.ReflectionSupport.invokeMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

class CartesianProductTestExtension implements TestTemplateInvocationContextProvider {

	@Override
	public boolean supportsTestTemplate(ExtensionContext context) {
		return findAnnotation(context.getTestMethod(), CartesianProductTest.class).isPresent();
	}

	@Override
	public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
		List<List<?>> sets = computeSets(context.getRequiredTestMethod());
		return cartesianProduct(sets).stream().map(CartesianProductTestInvocationContext::new);
	}

	private List<List<?>> computeSets(Method testMethod) {
		CartesianProductTest annotation = findAnnotation(testMethod, CartesianProductTest.class)
				.orElseThrow(() -> new AssertionError("@CartesianProductTest not found"));
		// Compute A ⨯ A ⨯ ... ⨯ A from single source "set"
		if (annotation.value().length > 0) {
			List<String> strings = Arrays.asList(annotation.value());
			List<List<?>> sets = new ArrayList<>();
			for (int i = 0; i < testMethod.getParameterTypes().length; i++) {
				sets.add(strings);
			}
			return sets;
		}
		// No single entry supplied? Try the sets factory method instead...
		String factoryMethod = annotation.factory().isEmpty() ? testMethod.getName() : annotation.factory();

		return invokeSetsFactory(testMethod, factoryMethod).getSets();
	}

	private CartesianProductTest.Sets invokeSetsFactory(Method testMethod, String factoryMethodName) {
		Class<?> declaringClass = testMethod.getDeclaringClass();
		Method factory = PioneerUtils
				.findMethodCurrentOrEnclosing(declaringClass, factoryMethodName)
				.orElseThrow(() -> new AssertionError("Method `CartesianProductTest.Sets " + factoryMethodName
						+ "()` not found in " + declaringClass + "or any enclosing class"));
		String method = "Method `" + factory + "`";
		if (!Modifier.isStatic(factory.getModifiers())) {
			throw new AssertionError(method + " must be static");
		}
		if (!CartesianProductTest.Sets.class.isAssignableFrom(factory.getReturnType())) {
			throw new AssertionError(method + " must return `CartesianProductTest.Sets`");
		}
		CartesianProductTest.Sets sets = (CartesianProductTest.Sets) invokeMethod(factory, null);
		if (sets.getSets().size() > testMethod.getParameterCount()) {
			throw new AssertionError(method + " must register values for each parameter exactly once");
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
