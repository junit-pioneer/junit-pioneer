/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.testkit;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.util.List;

import org.opentest4j.TestAbortedException;

public class PioneerTestKit {

	/**
	 * Returns the execution results of the given test class.
	 *
	 * @param testClass Name of the test class, the results should be returned
	 * @return The execution results
	 */
	public static ExecutionResults executeTestClass(Class<?> testClass) {
		return new ExecutionResults(testClass);
	}

	/**
	 * Returns the execution results of the given method of a given test class.
	 *
	 * @param testClass Name of the test class
	 * @param testMethodName Name of the test method (of the given class)
	 * @return The execution results
	 */
	public static ExecutionResults executeTestMethod(Class<?> testClass, String testMethodName) {
		return new ExecutionResults(testClass, testMethodName);
	}

	/**
	 * Returns the execution results of the given method of a given test class.
	 *
	 * @param testClass Name of the test class
	 * @param testMethodName Name of the test method (of the given class)
	 * @param methodParameterTypes Class type(s) of the parameter(s)
	 * @return The execution results
	 * @throws IllegalArgumentException when methodParameterTypes is null
	 * 			This method only checks parameters which are not part of the underlying
	 * 			Jupiter TestKit. The Jupiter TestKit may throw other exceptions!
	 */
	public static ExecutionResults executeTestMethodWithParameterTypes(Class<?> testClass, String testMethodName,
			Class<?>... methodParameterTypes) {

		String allTypeNames = toMethodParameterTypesString(methodParameterTypes);

		return new ExecutionResults(testClass, testMethodName, allTypeNames);
	}

	/**
	 * Returns the execution results of the given nested test class.
	 *
	 * @param enclosingClasses List of the enclosing classes
	 * @param testClass Name of the test class, the results should be returned
	 * @return The execution results
	 */
	public static ExecutionResults executeNestedTestClass(List<Class<?>> enclosingClasses, Class<?> testClass) {
		return new ExecutionResults(enclosingClasses, testClass);
	}

	/**
	 * Returns the execution results of the given method of a given nested test class.
	 *
	 * @param enclosingClasses List of the enclosing classes
	 * @param testClass Name of the test class
	 * @param testMethodName Name of the test method (of the given class)
	 * @return The execution results
	 */
	public static ExecutionResults executeNestedTestMethod(List<Class<?>> enclosingClasses, Class<?> testClass,
			String testMethodName) {
		return new ExecutionResults(enclosingClasses, testClass, testMethodName);
	}

	/**
	 * Returns the execution results of the given method of a given nested test class.
	 *
	 * @param enclosingClasses List of the enclosing classes
	 * @param testClass Name of the test class
	 * @param testMethodName Name of the test method (of the given class)
	 * @param methodParameterTypes Class type(s) of the parameter(s)
	 * @return The execution results
	 * @throws IllegalArgumentException when methodParameterTypes is null
	 *			This method only checks parameters which are not part of the underlying
	 *			Jupiter TestKit. The Jupiter TestKit may throw other exceptions!
	 */
	public static ExecutionResults executeNestedTestMethodWithParameterTypes(List<Class<?>> enclosingClasses,
			Class<?> testClass, String testMethodName, Class<?>... methodParameterTypes) {

		String allTypeNames = toMethodParameterTypesString(methodParameterTypes);

		return new ExecutionResults(enclosingClasses, testClass, testMethodName, allTypeNames);
	}

	private static String toMethodParameterTypesString(Class<?>... methodParameterTypes) {
		// throw IllegalArgumentException for a `null` array instead of NPE
		// (hence no use of `Objects::requireNonNull`)
		if (methodParameterTypes == null) {
			throw new IllegalArgumentException("methodParameterTypes must not be null");
		}

		// Concatenating all type names, because DiscoverySelectors.selectMethod only takes String as a parameter.
		return stream(methodParameterTypes).map(Class::getName).collect(joining(","));
	}

	/**
	 * Aborts the test execution. Makes the test code a little nicer.
	 *
	 * @throws TestAbortedException always throws this
	 */
	public static void abort() {
		throw new TestAbortedException();
	}

}
