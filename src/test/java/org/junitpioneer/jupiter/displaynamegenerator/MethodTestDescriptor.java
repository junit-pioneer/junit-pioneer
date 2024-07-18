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

import org.junit.jupiter.api.DisplayName;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.MethodSource;

public class MethodTestDescriptor extends AbstractTestDescriptor {

	private final Class<?> testClass;
	private final Method testMethod;

	public MethodTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method testMethod) {
		super(uniqueId,
			testMethod.isAnnotationPresent(DisplayName.class) ? testMethod.getAnnotation(DisplayName.class).value()
					: ReplaceCamelCaseAndUnderscoreAndNumber.INSTANCE
							.generateDisplayNameForMethod(testClass, testMethod),
			MethodSource.from(testClass, testMethod));
		this.testClass = testClass;
		this.testMethod = testMethod;
	}

	@Override
	public Type getType() {
		return Type.TEST;
	}

}
