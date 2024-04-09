/*
 * Copyright 2016-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.testkit;

public class TestSelector {

	private final Class<?> testClass;
	private final String testMethodName;
	private final String methodParameterTypes;

	public TestSelector(Class<?> testClass, String testMethodName, String methodParameterTypes) {
		this.testClass = testClass;
		this.testMethodName = testMethodName;
		this.methodParameterTypes = methodParameterTypes;
	}

	public Class<?> getTestClass() {
		return testClass;
	}

	public String getTestMethodName() {
		return testMethodName;
	}

	public String getMethodParameterTypes() {
		return methodParameterTypes;
	}

}
