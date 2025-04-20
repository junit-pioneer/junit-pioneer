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

public class RandomLongParameterProvider extends RandomBoundedParameterProvider<Long> {

	public RandomLongParameterProvider() {
		super(Long.MIN_VALUE, Long.MAX_VALUE);
	}

	@Override
	public List<Class<?>> getSupportedParameterTypes() {
		return List.of(Long.class, long.class);
	}

	@Override
	public Long getDefaultRandomNumber() {
		return this.random.nextLong();
	}

	@Override
	public Long provideRandomNumber(Long min, Long max) {
		return PioneerRandomUtils.boundedNextLong(random, min, max);
	}

}
