/*
 * Copyright 2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.random;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;

public abstract class RandomParameterProvider {

	/**
	 * Can be used by extending classes to check if jakarta validation is present.
	 * Check the documentation to see how default implementations use this field in Pioneer.
	 */
	protected static final boolean IS_JAKARTA_VALIDATION_PRESENT = isJakartaValidationClassPresent();

	/**
	 * A java.util.Random instance to use for generating random parameters.
	 * Note, that this is not a {@link java.security.SecureRandom} instance.
	 */
	protected Random random;
	protected ParameterContext parameterContext;
	protected ExtensionContext extensionContext;

	public RandomParameterProvider() {
		// recreate default constructor to prevent compiler warning
	}

	static boolean isJakartaValidationClassPresent() {
		try {
			RandomParameterProvider.class.getClassLoader().loadClass("jakarta.validation.Constraint");
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	public void init(Random random, ParameterContext parameterContext, ExtensionContext extensionContext) {
		this.random = random;
		this.parameterContext = parameterContext;
		this.extensionContext = extensionContext;
	}

	/**
	 * Get all the possible types that this provider can return.
	 */
	public abstract List<Class<?>> getSupportedParameterTypes();

	/**
	 * Creates a random parameter for a type given in {@link  RandomParameterProvider#getSupportedParameterTypes}.
	 * Based on the injection method the {@code Parameter} can be a constructor, setter or test parameter.
	 * If possible, (i.e.: can be found) the {@code Field} is the field corresponding to the setter/constructor
	 * parameter.
	 * <p/>
	 * Optionally, if the jakarta validation artifact is present on the classpath, an implementation may
	 * scan the field or parameter for constraint annotations.
	 *
	 * @param parameter the parameter of the test method or the parameter of the setter/constructor
	 *                  if the test parameter is a more complex type
	 * @param field     the field corresponding to the parameter, could be {@code null}
	 * @return a random parameter
	 * @see RandomParameterProvider#IS_JAKARTA_VALIDATION_PRESENT
	 */
	public abstract Object provideRandomParameter(Parameter parameter, Field field);

}
