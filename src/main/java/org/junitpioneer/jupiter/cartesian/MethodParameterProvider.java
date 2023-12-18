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
import static java.util.Arrays.stream;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junitpioneer.internal.PioneerPreconditions;

/**
 * {@link CartesianParameterArgumentsProvider} implementation for {@link CartesianTest.MethodParameterSource}.
 */
class MethodParameterProvider implements CartesianParameterArgumentsProvider<Object>,
		AnnotationConsumer<CartesianTest.MethodParameterSource> {

	private CartesianTest.MethodParameterSource source;

	MethodParameterProvider() {
	}

	@Override
	public void accept(final CartesianTest.MethodParameterSource source) {
		this.source = source;
	}

	@Override
	public Stream<Object> provideArguments(ExtensionContext context, Parameter parameter) {
		return this.provideArguments(context, this.source);
	}

	// Below is mostly adapted from MethodArgumentsProvider & it's dependencies (junit 5.10.1)

	private static final Predicate<Method> isFactoryMethod = //
		method -> isConvertibleToStream(method.getReturnType()) && !isTestMethod(method);

	protected Stream<Object> provideArguments(ExtensionContext context,
			CartesianTest.MethodParameterSource methodSource) {
		Class<?> testClass = context.getRequiredTestClass();
		Method testMethod = context.getRequiredTestMethod();
		Object testInstance = context.getTestInstance().orElse(null);
		String[] methodNames = methodSource.value();
		// @formatter:off
        return stream(methodNames)
            .map(factoryMethodName -> findFactoryMethod(testClass, testMethod, factoryMethodName))
            .map(factoryMethod -> validateFactoryMethod(factoryMethod, testInstance))
            .map(factoryMethod -> context.getExecutableInvoker().invoke(factoryMethod, testInstance))
            .flatMap(MethodParameterProvider::toStream);
        // @formatter:on
	}

	/**
	 * Determine if the supplied {@link String} is <em>blank</em> (i.e.,
	 * {@code null} or consisting only of whitespace characters).
	 *
	 * @param str the string to check; may be {@code null}
	 * @return {@code true} if the string is blank
	 */
	public static boolean isBlank(String str) {
		return (str == null || str.trim().isEmpty());
	}

	private static Method findFactoryMethod(Class<?> testClass, Method testMethod, String factoryMethodName) {
		String originalFactoryMethodName = factoryMethodName;

		// If the user did not provide a factory method name, find a "default" local
		// factory method with the same name as the parameterized test method.
		if (isBlank(factoryMethodName)) {
			factoryMethodName = testMethod.getName();
			return findFactoryMethodBySimpleName(testClass, testMethod, factoryMethodName);
		}

		// Convert local factory method name to fully-qualified method name.
		if (!looksLikeAFullyQualifiedMethodName(factoryMethodName)) {
			factoryMethodName = testClass.getName() + "#" + factoryMethodName;
		}

		// Find factory method using fully-qualified name.
		Method factoryMethod = findFactoryMethodByFullyQualifiedName(testClass, testMethod, factoryMethodName);

		// Ensure factory method has a valid return type and is not a test method.
		PioneerPreconditions
				.condition(isFactoryMethod.test(factoryMethod), () -> format(
					"Could not find valid factory method [%s] for test class [%s] but found the following invalid candidate: %s",
					originalFactoryMethodName, testClass.getName(), factoryMethod));

		return factoryMethod;
	}

	private static boolean looksLikeAFullyQualifiedMethodName(String factoryMethodName) {
		if (factoryMethodName.contains("#")) {
			return true;
		}
		int indexOfFirstDot = factoryMethodName.indexOf('.');
		if (indexOfFirstDot == -1) {
			return false;
		}
		int indexOfLastOpeningParenthesis = factoryMethodName.lastIndexOf('(');
		if (indexOfLastOpeningParenthesis > 0) {
			// Exclude simple/local method names with parameters
			return indexOfFirstDot < indexOfLastOpeningParenthesis;
		}
		// If we get this far, we conclude the supplied factory method name "looks"
		// like it was intended to be a fully-qualified method name, even if the
		// syntax is invalid. We do this in order to provide better diagnostics for
		// the user when a fully-qualified method name is in fact invalid.
		return true;
	}

	// package-private for testing
	static Method findFactoryMethodByFullyQualifiedName(Class<?> testClass, Method testMethod,
			String fullyQualifiedMethodName) {
		String[] methodParts = parseFullyQualifiedMethodName(fullyQualifiedMethodName);
		String className = methodParts[0];
		String methodName = methodParts[1];
		String methodParameters = methodParts[2];
		// ClassLoader classLoader = ClassLoaderUtils.getClassLoader(testClass);
		Class<?> clazz = loadRequiredClass(className); // , classLoader);

		// Attempt to find an exact match first.
		Method factoryMethod = ReflectionSupport.findMethod(clazz, methodName, methodParameters).orElse(null);
		if (factoryMethod != null) {
			return factoryMethod;
		}

		boolean explicitParameterListSpecified = //
			!isBlank(methodParameters) || fullyQualifiedMethodName.endsWith("()");

		// If we didn't find an exact match but an explicit parameter list was specified,
		// that's a user configuration error.
		PioneerPreconditions
				.condition(!explicitParameterListSpecified,
					() -> format("Could not find factory method [%s(%s)] in class [%s]", methodName, methodParameters,
						className));

		// Otherwise, fall back to the same lenient search semantics that are used
		// to locate a "default" local factory method.
		return findFactoryMethodBySimpleName(clazz, testMethod, methodName);
	}

	/**
	 * Find the factory method by searching for all methods in the given {@code clazz}
	 * with the desired {@code factoryMethodName} which have return types that can be
	 * converted to a {@link Stream}, ignoring the {@code testMethod} itself as well
	 * as any {@code @Test}, {@code @TestTemplate}, or {@code @TestFactory} methods
	 * with the same name.
	 *
	 * @return the single factory method matching the search criteria
	 * @throws PreconditionViolationException if the factory method was not found or
	 *                                        multiple competing factory methods with the same name were found
	 */
	private static Method findFactoryMethodBySimpleName(Class<?> clazz, Method testMethod, String factoryMethodName) {
		Predicate<Method> isCandidate = candidate -> factoryMethodName.equals(candidate.getName())
				&& !testMethod.equals(candidate);
		List<Method> candidates = ReflectionSupport.findMethods(clazz, isCandidate, HierarchyTraversalMode.TOP_DOWN);

		List<Method> factoryMethods = candidates.stream().filter(isFactoryMethod).collect(toList());

		PioneerPreconditions.condition(factoryMethods.size() > 0, () -> {
			// If we didn't find the factory method using the isFactoryMethod Predicate, perhaps
			// the specified factory method has an invalid return type or is a test method.
			// In that case, we report the invalid candidates that were found.
			if (candidates.size() > 0) {
				return format(
					"Could not find valid factory method [%s] in class [%s] but found the following invalid candidates: %s",
					factoryMethodName, clazz.getName(), candidates);
			}
			// Otherwise, report that we didn't find anything.
			return format("Could not find factory method [%s] in class [%s]", factoryMethodName, clazz.getName());
		});
		PioneerPreconditions
				.condition(factoryMethods.size() == 1,
					() -> format("%d factory methods named [%s] were found in class [%s]: %s", factoryMethods.size(),
						factoryMethodName, clazz.getName(), factoryMethods));
		return factoryMethods.get(0);
	}

	private static boolean isTestMethod(Method candidate) {
		return isAnnotated(candidate, Test.class) || isAnnotated(candidate, TestTemplate.class)
				|| isAnnotated(candidate, TestFactory.class);
	}

	private static Class<?> loadRequiredClass(String className) { //, ClassLoader classLoader) {
		return ReflectionSupport
				.tryToLoadClass(className)
				.getOrThrow( //, classLoader).getOrThrow(
					cause -> new JUnitException(format("Could not load class [%s]", className), cause));
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

	/**
	 * Determine if an instance of the supplied type can be converted into a
	 * {@code Stream}.
	 *
	 * <p>If this method returns {@code true}, {@link #toStream(Object)} can
	 * successfully convert an object of the specified type into a stream. See
	 * {@link #toStream(Object)} for supported types.
	 *
	 * @param type the type to check; may be {@code null}
	 * @return {@code true} if an instance of the type can be converted into a stream
	 * @see #toStream(Object)
	 */
	public static boolean isConvertibleToStream(Class<?> type) {
		if (type == null || type == void.class) {
			return false;
		}
		return (Stream.class.isAssignableFrom(type)//
				|| DoubleStream.class.isAssignableFrom(type)//
				|| IntStream.class.isAssignableFrom(type)//
				|| LongStream.class.isAssignableFrom(type)//
				|| Iterable.class.isAssignableFrom(type)//
				|| Iterator.class.isAssignableFrom(type)//
				|| Object[].class.isAssignableFrom(type)//
				|| (type.isArray() && type.getComponentType().isPrimitive()));
	}

	/**
	 * Convert an object of one of the following supported types into a {@code Stream}.
	 *
	 * <ul>
	 * <li>{@link Stream}</li>
	 * <li>{@link DoubleStream}</li>
	 * <li>{@link IntStream}</li>
	 * <li>{@link LongStream}</li>
	 * <li>{@link Collection}</li>
	 * <li>{@link Iterable}</li>
	 * <li>{@link Iterator}</li>
	 * <li>{@link Object} array</li>
	 * <li>primitive array</li>
	 * </ul>
	 *
	 * @param object the object to convert into a stream; never {@code null}
	 * @return the resulting stream
	 * @throws PreconditionViolationException if the supplied object is {@code null}
	 *                                        or not one of the supported types
	 * @see #isConvertibleToStream(Class)
	 */
	public static Stream<?> toStream(Object object) {
		PioneerPreconditions.notNull(object, "Object must not be null");
		if (object instanceof Stream) {
			return (Stream<?>) object;
		}
		if (object instanceof DoubleStream) {
			return ((DoubleStream) object).boxed();
		}
		if (object instanceof IntStream) {
			return ((IntStream) object).boxed();
		}
		if (object instanceof LongStream) {
			return ((LongStream) object).boxed();
		}
		if (object instanceof Collection) {
			return ((Collection<?>) object).stream();
		}
		if (object instanceof Iterable) {
			return StreamSupport.stream(((Iterable<?>) object).spliterator(), false);
		}
		if (object instanceof Iterator) {
			return StreamSupport.stream(spliteratorUnknownSize((Iterator<?>) object, ORDERED), false);
		}
		if (object instanceof Object[]) {
			return Arrays.stream((Object[]) object);
		}
		if (object instanceof double[]) {
			return DoubleStream.of((double[]) object).boxed();
		}
		if (object instanceof int[]) {
			return IntStream.of((int[]) object).boxed();
		}
		if (object instanceof long[]) {
			return LongStream.of((long[]) object).boxed();
		}
		if (object.getClass().isArray() && object.getClass().getComponentType().isPrimitive()) {
			return IntStream.range(0, Array.getLength(object)).mapToObj(i -> Array.get(object, i));
		}
		throw new PreconditionViolationException(
			"Cannot convert instance of " + object.getClass().getName() + " into a Stream: " + object);
	}

	/**
	 * Parse the supplied <em>fully qualified method name</em> into a 3-element
	 * {@code String[]} with the following content.
	 *
	 * <ul>
	 *   <li>index {@code 0}: the fully qualified class name</li>
	 *   <li>index {@code 1}: the name of the method</li>
	 *   <li>index {@code 2}: a comma-separated list of parameter types, or a
	 *       blank string if the method does not declare any formal parameters</li>
	 * </ul>
	 *
	 * @param fullyQualifiedMethodName a <em>fully qualified method name</em>,
	 *                                 never {@code null} or blank
	 * @return a 3-element array of strings containing the parsed values
	 */
	public static String[] parseFullyQualifiedMethodName(String fullyQualifiedMethodName) {
		PioneerPreconditions.notBlank(fullyQualifiedMethodName, "fullyQualifiedMethodName must not be null or blank");

		int indexOfFirstHashtag = fullyQualifiedMethodName.indexOf('#');
		boolean validSyntax = (indexOfFirstHashtag > 0)
				&& (indexOfFirstHashtag < fullyQualifiedMethodName.length() - 1);

		PioneerPreconditions
				.condition(validSyntax, () -> "[" + fullyQualifiedMethodName
						+ "] is not a valid fully qualified method name: "
						+ "it must start with a fully qualified class name followed by a '#' "
						+ "and then the method name, optionally followed by a parameter list enclosed in parentheses.");

		String className = fullyQualifiedMethodName.substring(0, indexOfFirstHashtag);
		String methodPart = fullyQualifiedMethodName.substring(indexOfFirstHashtag + 1);
		String methodName = methodPart;
		String methodParameters = "";

		if (methodPart.endsWith("()")) {
			methodName = methodPart.substring(0, methodPart.length() - 2);
		} else if (methodPart.endsWith(")")) {
			int indexOfLastOpeningParenthesis = methodPart.lastIndexOf('(');
			if ((indexOfLastOpeningParenthesis > 0) && (indexOfLastOpeningParenthesis < methodPart.length() - 1)) {
				methodName = methodPart.substring(0, indexOfLastOpeningParenthesis);
				methodParameters = methodPart.substring(indexOfLastOpeningParenthesis + 1, methodPart.length() - 1);
			}
		}
		return new String[] { className, methodName, methodParameters };
	}

}
