/*
 * Copyright 2016-2022 the original author or authors.
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
import java.util.Arrays;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.support.AnnotationConsumer;

public class NumberArgumentProvider {

	// tag::cartesian_number_argument_provider[]
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@CartesianArgumentsSource(NumberArgumentsProvider.class)
	public @interface NumberSource {

		int[] value();

	}

	class NumberArgumentsProvider implements CartesianMethodArgumentsProvider, AnnotationConsumer<NumberSource> {

		private int[] numbers;

		@Override
		public ArgumentSets provideArguments(ExtensionContext context) {
			int paramCount = context.getRequiredTestMethod().getParameters().length;
			ArgumentSets sets = ArgumentSets.create();
			for (int i = 0; i < paramCount; i++) {
				sets.argumentsForNextParameter(Arrays.stream(numbers).boxed());
			}
			return sets;
		}

		@Override
		public void accept(NumberSource source) {
			this.numbers = source.value();
		}

	}

	// end::cartesian_number_argument_provider[]
}
