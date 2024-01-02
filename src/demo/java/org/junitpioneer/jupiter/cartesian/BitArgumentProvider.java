/*
 * Copyright 2016-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.cartesian;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtensionContext;

public class BitArgumentProvider {

	// tag::cartesian_bit_source_annotation[]
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@CartesianArgumentsSource(BitArgumentsProvider.class)
	public @interface BitSource {
	}
	// end::cartesian_bit_source_annotation[]

	// tag::cartesian_bit_argument_provider[]
	class BitArgumentsProvider implements CartesianMethodArgumentsProvider {

		@Override
		public ArgumentSets provideArguments(ExtensionContext context) {
			int paramCount = context.getRequiredTestMethod().getParameters().length;
			ArgumentSets sets = ArgumentSets.create();
			for (int i = 0; i < paramCount; i++) {
				sets.argumentsForNextParameter(0, 1);
			}
			return sets;
		}

	}

	// end::cartesian_bit_argument_provider[]
}
