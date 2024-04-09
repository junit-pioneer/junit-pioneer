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

import java.util.List;

public class NestedTestSelector extends TestSelector {

	private final List<Class<?>> enclosingClasses;

	public NestedTestSelector(List<Class<?>> enclosingClasses, Class<?> testClass, String testMethodName,
			String methodParameterTypes) {
		super(testClass, testMethodName, methodParameterTypes);
		this.enclosingClasses = enclosingClasses;
	}

	public List<Class<?>> getEnclosingClasses() {
		return enclosingClasses;
	}

}
