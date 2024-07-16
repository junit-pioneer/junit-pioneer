/*
 * Copyright 2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.displaynamegenerator;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;

class ClassTestDescriptor extends AbstractTestDescriptor {

	private final Class<?> testClass;

	public ClassTestDescriptor(Class<?> testClass, TestDescriptor parent) {
		super( //
			parent.getUniqueId().append("class", testClass.getName()), //
			ReplaceCamelCaseAndUnderscoreAndNumber.INSTANCE.generateDisplayNameForClass(testClass), //
			ClassSource.from(testClass) //
		);
		this.testClass = testClass;
		setParent(parent);
		addAllChildren();
	}

	@Override
	public Type getType() {
		return Type.CONTAINER;
	}

	private void addAllChildren() {
		// Add method descriptors as children
		for (Method method : testClass.getDeclaredMethods()) {
			if (method.isAnnotationPresent(Test.class) || method.isAnnotationPresent(ParameterizedTest.class)) {
				UniqueId methodId = getUniqueId().append("method", method.getName());
				TestDescriptor methodDescriptor = new MethodTestDescriptor(methodId, testClass, method);
				addChild(methodDescriptor);
			}
		}
	}

}
