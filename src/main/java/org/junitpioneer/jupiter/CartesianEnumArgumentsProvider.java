/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * This is basically a copy of Jupiter's {@code EnumArgumentsProvider},
 * except it does NOT support {@code @ParameterizedTest}.
 */
class CartesianEnumArgumentsProvider implements CartesianAnnotationConsumer<CartesianEnumSource>, ArgumentsProvider {

	private EnumSet<?> constants;

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void accept(CartesianEnumSource enumSource) {
		Class enumClass = enumSource.value();
		this.constants = EnumSet.allOf(enumClass);

		CartesianEnumSource.Mode mode = enumSource.mode();
		String[] declaredConstantNames = enumSource.names();
		if (declaredConstantNames.length > 0) {
			Set<String> uniqueNames = stream(declaredConstantNames).collect(toSet());

			if (uniqueNames.size() != declaredConstantNames.length)
				throw new PreconditionViolationException("Duplicate enum constant name(s) found in " + enumSource);

			mode.validate(enumSource, uniqueNames);
			this.constants.removeIf(constant -> !mode.select(constant, uniqueNames));
		}
	}

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		return constants.stream().map(Arguments::of);
	}

}
