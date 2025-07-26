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

public class RandomByteParameterProvider extends RandomBoundedParameterProvider<Byte> {

	public RandomByteParameterProvider() {
		super((long) Byte.MIN_VALUE, (long) Byte.MAX_VALUE);
	}

	@Override
	public List<Class<?>> getSupportedParameterTypes() {
		return List.of(byte.class, Byte.class);
	}

	@Override
	public Byte provideRandomNumber(Long min, Long max) {
		return (byte) PioneerRandomUtils.boundedNextLong(random, min, max);
	}

}
