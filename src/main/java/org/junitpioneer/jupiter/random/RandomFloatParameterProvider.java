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

import java.util.List;

public class RandomFloatParameterProvider extends RandomNumberProvider<Float> {

	public RandomFloatParameterProvider() {
		super(Long.MIN_VALUE, Long.MAX_VALUE);
	}

	@Override
	public List<Class<?>> getSupportedParameterTypes() {
		return List.of(Float.class, float.class);
	}

	@Override
	public Float getDefaultRandomNumber() {
		return this.random.nextFloat();
	}

	@Override
	public Float provideRandomNumber(Long min, Long max) {
		return this.random.nextFloat(min, max);
	}

}