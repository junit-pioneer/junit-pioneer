/*
 * Copyright 2026 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.cartesian;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junitpioneer.internal.PioneerPreconditions;
import org.junitpioneer.internal.PioneerUtils;

/**
 * {@link CartesianParameterArgumentsProvider} implementation for {@link CartesianTest.MethodParameterSource}.
 */
class MethodParameterProvider implements CartesianParameterArgumentsProvider<Object>,
		AnnotationConsumer<CartesianTest.MethodParameterSource> {

	private static final Predicate<Method> isFactoryMethod = //
		method -> PioneerUtils.isConvertibleToStream(method.getReturnType()) && !isTestMethod(method);

	private CartesianTest.MethodParameterSource source;

	MethodParameterProvider() {
	}

	@Override
	public void accept(final CartesianTest.MethodParameterSource source) {
		this.source = source;
	}

	@Override
	public Stream<Object> provideArguments(ExtensionContext context, Parameter parameter) {
		Class<?> testClass = context.getRequiredTestClass();
		Method testMethod = context.getRequiredTestMethod();
		Object testInstance = context.getTestInstance().orElse(null);
		return stream(source.value())
				.map(factoryMethodName -> findFactoryMethod(testClass, testMethod, factoryMethodName))
				.map(factoryMethod -> validateFactoryMethod(factoryMethod, testInstance))
				.map(factoryMethod -> context.getExecutableInvoker().invoke(factoryMethod, testInstance))
				.flatMap(PioneerUtils::toStream);
	}

	/**
	 * Find the factory method referenced by {@code factoryMethodName}, which has the
	 * form {@code [className#]methodName[(parameters)]}.
	 *
	 * <p>If a class name is given, only that class is searched. Otherwise, the test
	 * class and then each of its enclosing classes (outwards) are searched.
	 *
	 * <p>If a parameter list is given (even an empty one), the method must match it
	 * exactly. Otherwise, a method with that name and without parameters is preferred,
	 * falling back to the single method with that name, a return type that can be
	 * converted to a {@link Stream}, and no test annotation.
	 */
	private static Method findFactoryMethod(Class<?> testClass, Method testMethod, String factoryMethodName) {
		String methodName = MemberNameUtils
				.extractMethodName(factoryMethodName)
				.orElseThrow(() -> new PreconditionViolationException(
					format("Could not extract a factory method name from [%s]", factoryMethodName)));
		Optional<String> parameterTypes = MemberNameUtils.extractParameterTypes(factoryMethodName);
		Optional<String> className = MemberNameUtils.extractClassName(factoryMethodName);
		List<Class<?>> classesToSearch = classesToSearch(testClass, testMethod, className);

		String methodDisplayName = methodName + parameterTypes.map(types -> "(" + types + ")").orElse("");
		Method factoryMethod = classesToSearch
				.stream()
				.map(clazz -> findFactoryMethodInClass(clazz, testMethod, methodName, parameterTypes))
				.flatMap(Optional::stream)
				.findFirst()
				.orElseThrow(() -> new PreconditionViolationException(className.isPresent()
						? format("Could not find factory method [%s] in class [%s]", methodDisplayName,
							classesToSearch.get(0).getName())
						: format("Could not find factory method [%s] in class [%s] or any enclosing class",
							methodDisplayName, testClass.getName())));

		// An exact match (by explicit parameter list or without parameters) hasn't been checked
		// for return type and test annotations yet.
		PioneerPreconditions
				.condition(isFactoryMethod.test(factoryMethod), () -> format(
					"Could not find valid factory method [%s] for test class [%s] but found the following invalid candidate: %s",
					factoryMethodName, testClass.getName(), factoryMethod));

		return factoryMethod;
	}

	private static List<Class<?>> classesToSearch(Class<?> testClass, Method testMethod, Optional<String> className) {
		if (className.isEmpty()) {
			return currentAndEnclosingClasses(testClass);
		}

		String name = className.get();
		Class<?> explicitClass = PioneerUtils
				.findClassCurrentOrEnclosing(testClass, name)
				.orElseThrow(() -> new PreconditionViolationException(
					format("Class %s not found, referenced in method %s", name, testMethod.getName())));
		return List.of(explicitClass);
	}

	private static List<Class<?>> currentAndEnclosingClasses(Class<?> clazz) {
		List<Class<?>> classes = new ArrayList<>();
		for (Class<?> current = clazz; current != null; current = current.getEnclosingClass()) {
			classes.add(current);
		}
		return classes;
	}

	private static Optional<Method> findFactoryMethodInClass(Class<?> clazz, Method testMethod, String methodName,
			Optional<String> parameterTypes) {
		// An explicit parameter list must match exactly. Without one, prefer a method without parameters
		// (like JUnit's @MethodSource) and fall back to a lenient search among methods with that name.
		Optional<Method> exactMatch = ReflectionSupport.findMethod(clazz, methodName, parameterTypes.orElse(""));
		if (exactMatch.isPresent() || parameterTypes.isPresent()) {
			return exactMatch;
		}
		return findFactoryMethodBySimpleName(clazz, testMethod, methodName);
	}

	/**
	 * Find the factory method by searching for all methods in the given {@code clazz}
	 * with the desired {@code factoryMethodName} which have return types that can be
	 * converted to a {@link Stream}, ignoring the {@code testMethod} itself as well
	 * as any {@code @Test}, {@code @TestTemplate}, or {@code @TestFactory} methods
	 * with the same name.
	 *
	 * @return the single factory method matching the search criteria, or an empty
	 *         {@code Optional} if the class has no method with that name
	 * @throws PreconditionViolationException if methods with that name were found, but
	 *                                        none or more than one of them is a valid factory method
	 */
	private static Optional<Method> findFactoryMethodBySimpleName(Class<?> clazz, Method testMethod,
			String factoryMethodName) {
		Predicate<Method> isCandidate = candidate -> factoryMethodName.equals(candidate.getName())
				&& !testMethod.equals(candidate);
		List<Method> candidates = ReflectionSupport.findMethods(clazz, isCandidate, HierarchyTraversalMode.TOP_DOWN);
		if (candidates.isEmpty()) {
			return Optional.empty();
		}

		List<Method> factoryMethods = candidates.stream().filter(isFactoryMethod).collect(toList());
		// If we didn't find the factory method using the isFactoryMethod Predicate, perhaps
		// the specified factory method has an invalid return type or is a test method.
		// In that case, we report the invalid candidates that were found.
		PioneerPreconditions
				.condition(!factoryMethods.isEmpty(), () -> format(
					"Could not find valid factory method [%s] in class [%s] but found the following invalid candidates: %s",
					factoryMethodName, clazz.getName(), candidates));
		PioneerPreconditions
				.condition(factoryMethods.size() == 1,
					() -> format("%d factory methods named [%s] were found in class [%s]: %s", factoryMethods.size(),
						factoryMethodName, clazz.getName(), factoryMethods));
		return Optional.of(factoryMethods.get(0));
	}

	private static boolean isTestMethod(Method candidate) {
		return isAnnotated(candidate, Test.class) || isAnnotated(candidate, TestTemplate.class)
				|| isAnnotated(candidate, TestFactory.class);
	}

	private static Method validateFactoryMethod(Method factoryMethod, Object testInstance) {
		PioneerPreconditions
				.condition(
					factoryMethod.getDeclaringClass().isInstance(testInstance)
							|| Modifier.isStatic(factoryMethod.getModifiers()),
					() -> format("Method '%s' must be static: local factory methods must be static "
							+ "unless the PER_CLASS @TestInstance lifecycle mode is used; "
							+ "external factory methods must always be static.",
						factoryMethod.toGenericString()));
		return factoryMethod;
	}

}
