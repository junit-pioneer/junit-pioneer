/*
 * Copyright 2025 the original author or authors.
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
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junitpioneer.internal.PioneerRandomUtils;

public class RandomStringParameterProvider extends RandomParameterProvider {

	public RandomStringParameterProvider() {
		// recreate default constructor to prevent compiler warning
	}

	@Override
	public List<Class<?>> getSupportedParameterTypes() {
		return List.of(String.class, CharSequence.class);
	}

	@Override
	public Object provideRandomParameter(Parameter parameter, Field field) {
		if (IS_JAKARTA_VALIDATION_PRESENT) {
			// validation stuff
		}
		int length = PioneerRandomUtils.boundedNextInt(random, 3, 8);
		byte[] bytes = new byte[length];
		random.nextBytes(bytes);
		return new String(bytes, StandardCharsets.UTF_8);
	}

}
