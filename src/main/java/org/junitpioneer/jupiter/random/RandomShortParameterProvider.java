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

import java.util.List;

import org.junitpioneer.internal.PioneerRandomUtils;

public class RandomShortParameterProvider extends RandomBoundedParameterProvider<Short> {

	public RandomShortParameterProvider() {
		super((long) Short.MIN_VALUE, (long) Short.MAX_VALUE);
	}

	@Override
	public Short getDefaultRandomNumber() {
		return (short) PioneerRandomUtils.boundedNextInt(random, Short.MIN_VALUE, Short.MAX_VALUE);
	}

	@Override
	public Short provideRandomNumber(Long min, Long max) {
		int shortMin = (int) Math.max(min, Short.MIN_VALUE);
		int shortMax = (int) Math.min(max, Short.MAX_VALUE);
		return (short) PioneerRandomUtils.boundedNextInt(random, shortMin, shortMax);
	}

	@Override
	public List<Class<?>> getSupportedParameterTypes() {
		return List.of(short.class, Short.class);
	}

}
