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

public abstract class RandomParameterProvider {

	/**
	 * Can be used by extending classes to check if jakarta validation is present.
	 * Check the documentation to see how default implementations use this field in Pioneer.
	 */
	protected static final boolean IS_JAKARTA_VALIDATION_PRESENT = isJakartaValidationClassPresent();
	protected Random random;

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

	public void init(Random random) {
		this.random = random;
	}

	public abstract List<Class<?>> getSupportedParameterTypes();

	/**
	 * Creates a random parameter for a type given in {@link  RandomParameterProvider#getSupportedParameterTypes}.
	 * Based on the injection method the {@code Parameter} can be a constructor, setter or test parameter.
	 * If possible, (i.e.: can be found) the {@code Field} is the field corresponding to the setter/constructor
	 * parameter.
	 *
	 * @param parameter the parameter of the test method or the parameter of the setter/constructor
	 *                  if the test parameter is a more complex type
	 * @param field     the field corresponding to the parameter, could be {@code null}
	 * @return a random parameter
	 */
	public abstract Object provideRandomParameter(Parameter parameter, Field field);

}
