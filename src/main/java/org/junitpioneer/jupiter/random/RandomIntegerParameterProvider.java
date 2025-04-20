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

import org.junitpioneer.internal.PioneerRandomUtils;

public class RandomIntegerParameterProvider extends RandomBoundedParameterProvider<Integer> {

	public RandomIntegerParameterProvider() {
		super((long) Integer.MIN_VALUE, (long) Integer.MAX_VALUE);
	}

	@Override
	public List<Class<?>> getSupportedParameterTypes() {
		return List.of(Integer.class, int.class);
	}

	@Override
	public Integer getDefaultRandomNumber() {
		return this.random.nextInt();
	}

	@Override
	public Integer provideRandomNumber(Long min, Long max) {
		return PioneerRandomUtils.boundedNextInt(random, min.intValue(), max.intValue());
	}

}
