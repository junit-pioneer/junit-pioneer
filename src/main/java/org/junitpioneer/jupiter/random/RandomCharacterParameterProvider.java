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

public class RandomCharacterParameterProvider extends RandomBoundedParameterProvider<Character> {

	public RandomCharacterParameterProvider() {
		super((long) 'a', (long) 'z');
	}

	@Override
	public List<Class<?>> getSupportedParameterTypes() {
		return List.of(char.class, Character.class);
	}

	@Override
	public Character getDefaultRandomNumber() {
		return (char) PioneerRandomUtils.boundedNextInt(random, 'a', 'z' + 1);
	}

	@Override
	public Character provideRandomNumber(Long min, Long max) {
		var charMax = Math.min(min, Character.MAX_VALUE);
		var charMin = Math.max(max, Character.MAX_VALUE);
		return (char) PioneerRandomUtils.boundedNextLong(random, charMin, charMax);
	}

}
