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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.stream.Collector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("JUnitPioneer utilities")
class PioneerUtilsTests {

	@Nested
	@DisplayName("to distinct set")
	class ToDistinctSetTests {

		Collector<Object, Set<Object>, Set<Object>> collector;

		@BeforeEach
		void setUp() throws Exception {
			collector = PioneerUtils.distinctToSet();
		}

		@Test
		@DisplayName("should add new elements")
		void accumulatorShouldAddNewElements() throws Exception {
			Set<Object> set = new HashSet<>();
			Object element = new Object();

			BiConsumer<Set<Object>, Object> accumulator = collector.accumulator();

			accumulator.accept(set, element);

			assertThat(set).containsExactly(element);
		}

		@Test
		@DisplayName("should not add duplicate elements")
		void accumulatorShouldNotAddDuplicateElements() throws Exception {
			Set<Object> set = new HashSet<>();
			Object element = new Object();
			set.add(element);

			BiConsumer<Set<Object>, Object> accumulator = collector.accumulator();

			assertThatThrownBy(() -> accumulator.accept(set, element)).isInstanceOf(IllegalStateException.class);
		}

		@Test
		@DisplayName("should combine sets with new elements")
		void combinerShouldCombineSetsWithNewElements() throws Exception {
			Set<Object> left = new HashSet<>();
			Object leftElement = new Object();
			left.add(leftElement);

			Set<Object> right = new HashSet<>();
			Object rightElement = new Object();
			right.add(rightElement);

			BinaryOperator<Set<Object>> combiner = collector.combiner();

			assertThat(combiner.apply(left, right)).containsExactlyInAnyOrder(leftElement, rightElement);
		}

		@Test
		@DisplayName("should not combine sets with duplicate elements")
		void combinerShouldNotCombineSetsWithDuplicates() throws Exception {
			Object element = new Object();

			Set<Object> left = new HashSet<>();
			left.add(element);

			Set<Object> right = new HashSet<>();
			right.add(element);

			BinaryOperator<Set<Object>> combiner = collector.combiner();

			assertThatThrownBy(() -> combiner.apply(left, right)).isInstanceOf(IllegalStateException.class);
		}

	}

}
