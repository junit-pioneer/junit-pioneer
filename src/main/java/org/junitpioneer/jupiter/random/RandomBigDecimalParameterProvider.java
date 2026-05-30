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

import java.math.BigDecimal;
import java.util.List;

import org.junitpioneer.internal.PioneerRandomUtils;

public class RandomBigDecimalParameterProvider extends RandomBoundedParameterProvider<BigDecimal> {

	public RandomBigDecimalParameterProvider() {
		super(Long.MIN_VALUE, Long.MAX_VALUE);
	}

	@Override
	public BigDecimal provideRandomNumber(Long min, Long max) {
		return BigDecimal.valueOf(PioneerRandomUtils.boundedNextLong(random, min, max));
	}

	@Override
	public List<Class<?>> getSupportedParameterTypes() {
		return List.of(BigDecimal.class);
	}

}
