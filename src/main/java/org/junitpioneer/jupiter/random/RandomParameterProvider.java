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

import static java.lang.String.format;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.junit.jupiter.api.extension.ExtensionConfigurationException;

public abstract class RandomParameterProvider {

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
	 * TODO needs proper documentation!
	 * @param parameter parameter
	 * @param field field
	 * @return a random parameter
	 */
	public abstract Object provideRandomParameter(Parameter parameter, Field field);

	/**
	 * Convenience method for classes extending {@code RandomParameterProvider}.
	 * Can be used to verify that at most one of the optional annotations passed to this method are present.
	 *
	 * @param annotations a vararg array of {@link Optional} annotations, only one can be present.
	 */
	@SafeVarargs
	@SuppressWarnings("varargs")
	protected static void ensureAtMostOneConstraintIsActive(Optional<? extends Annotation>... annotations) {
		var all = Arrays.asList(annotations);
		if (all.stream().filter(Optional::isPresent).count() > 1) {
			throw new ExtensionConfigurationException(
				format("At most one of these annotations can be present on a given field or parameter: %s", all));
		}
	}

}
