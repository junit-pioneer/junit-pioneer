/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.testkit;

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
	 * @param methodParameterTypes Full qualified types names of the parameters (e.g. "java.nio.file.Path")
	 * @return The execution results
	 */
	public static ExecutionResults executeTestMethodWithParameterTypes(Class<?> testClass, String testMethodName,
			String methodParameterTypes) {
		return new ExecutionResults(testClass, testMethodName, methodParameterTypes);
	}

}
