/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.list;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The sets for CartesianProductTest")
class CartesianProductTestSetsTests {

	CartesianProductTest.Sets sets = new CartesianProductTest.Sets();

	@Test
	@DisplayName("should add distinct elements")
	void shouldAddDistinct() {
		List<Integer> list = list(4, 5, 6);
		Stream<Integer> stream = Stream.of(7, 8, 9);

		sets.add(1, 2, 3).addAll(list).addAll(stream);

		assertThat(sets.getSets()).containsExactly(list(1, 2, 3), list, list(7, 8, 9));
	}

	@Test
	@DisplayName("should remove non-distinct elements")
	void shouldRemoveNonDistinct() {
		List<Integer> list = list(4, 5, 4);
		Stream<Integer> stream = Stream.of(7, 8, 7);

		sets.add(1, 2, 1).addAll(list).addAll(stream);

		assertThat(sets.getSets()).containsExactly(list(1, 2), list(4, 5), list(7, 8));
	}

}
