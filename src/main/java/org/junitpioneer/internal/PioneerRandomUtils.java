/*
 * Copyright 2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.internal;

import static java.lang.String.format;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

public final class PioneerRandomUtils {

	private static final List<Class<?>> SUPPORTED_TYPES = List
			.of(int.class, Integer.class, boolean.class, Boolean.class);

	private PioneerRandomUtils() {
		// private constructor to prevent instantiation of utility class
	}

	public static boolean isSupportedType(Class<?> clazz) {
		return SUPPORTED_TYPES.stream().anyMatch(clazz::isAssignableFrom);
	}

	public static Object randomObject(Class<?> type, Random random) {
		if (type.isAssignableFrom(int.class) || type.isAssignableFrom(Integer.class)) {
			return randomInt(random);
		}
		if (type.isAssignableFrom(boolean.class) || type.isAssignableFrom(Boolean.class)) {
			return randomBoolean(random);
		}
		throw new IllegalArgumentException(
			format("Unsupported type [%s] for PioneerRandomUtils, supported types are: [%s]", type, SUPPORTED_TYPES));
	}

	private static int randomInt(Random random) {
		return Math.abs(random.nextInt() % 100);
	}

	private static boolean randomBoolean(Random random) {
		return random.nextBoolean();
	}

	private static LocalDateTime randomDateTime(Random random) {

	}

}
