/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.cartesian;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;

public interface CartesianMethodArgumentsProvider extends CartesianArgumentsProvider {

	Sets provideArguments(ExtensionContext context) throws Exception;

	class Sets {

		List<List<?>> sets = new ArrayList<>(); // NOSONAR

		public Sets add(Collection<?> items) {
			sets.add(new ArrayList<>(items));
			return this;
		}

		public Sets add(Object... items) {
			return add(Arrays.asList(items));
		}

		public Sets add(Stream<?> items) {
			return add(items.collect(Collectors.toList()));
		}

		List<List<?>> get() {
			return sets;
		}

	}

}
