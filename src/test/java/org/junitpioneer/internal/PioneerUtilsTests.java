/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Optional;
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
		void setUp() {
			collector = PioneerUtils.distinctToSet();
		}

		@Test
		@DisplayName("should add new elements")
		void accumulatorShouldAddNewElements() {
			Set<Object> set = new HashSet<>();
			Object element = new Object();

			BiConsumer<Set<Object>, Object> accumulator = collector.accumulator();

			accumulator.accept(set, element);

			assertThat(set).containsExactly(element);
		}

		@Test
		@DisplayName("should not add duplicate elements")
		void accumulatorShouldNotAddDuplicateElements() {
			Set<Object> set = new HashSet<>();
			Object element = new Object();
			set.add(element);

			BiConsumer<Set<Object>, Object> accumulator = collector.accumulator();

			assertThatThrownBy(() -> accumulator.accept(set, element)).isInstanceOf(IllegalStateException.class);
		}

		@Test
		@DisplayName("should combine sets with new elements")
		void combinerShouldCombineSetsWithNewElements() {
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
		void combinerShouldNotCombineSetsWithDuplicates() {
			Object element = new Object();

			Set<Object> left = new HashSet<>();
			left.add(element);

			Set<Object> right = new HashSet<>();
			right.add(element);

			BinaryOperator<Set<Object>> combiner = collector.combiner();

			assertThatThrownBy(() -> combiner.apply(left, right)).isInstanceOf(IllegalStateException.class);
		}

	}

	@Nested
	@DisplayName("findMethodCurrentOrEnclosing")
	class MethodFinderTests {

		@Test
		@DisplayName("finds method in current class")
		void findsItInCurrent() {
			Optional<Method> result = PioneerUtils
					.findMethodCurrentOrEnclosing(MethodFinderTestCases.class, "baseMethod");

			assertThat(result).isPresent();
		}

		@Test
		@DisplayName("returns empty Optional if method is not present")
		void returnsEmptyOptional() {
			Optional<Method> result = PioneerUtils
					.findMethodCurrentOrEnclosing(MethodFinderTestCases.class, "missingMethod");

			assertThat(result).isNotPresent();
		}

		@Test
		@DisplayName("finds method in parent class if it's not in the given class")
		void findsItInParent() {
			Optional<Method> result = PioneerUtils
					.findMethodCurrentOrEnclosing(
						MethodFinderTestCases.MethodFinderTestCasesChild.MethodFinderTestCasesGrandChild.class,
						"baseMethod");

			assertThat(result).isPresent();
		}

		@Test
		@DisplayName("does not check subclasses")
		void noSubclassCheck() {
			Optional<Method> result = PioneerUtils
					.findMethodCurrentOrEnclosing(MethodFinderTestCases.MethodFinderTestCasesChild.class,
						"grandchildrenMethod");

			assertThat(result).isNotPresent();
		}

	}

	@Nested
	@DisplayName("nullSafeToString")
	class NullSafeToStringTests {

		@Test
		@DisplayName("returns 'null' as String for null")
		void nullString() {
			String result = PioneerUtils.nullSafeToString(null);

			assertThat(result).isEqualTo("null");
		}

		@Test
		@DisplayName("returns Object.toString() for an object")
		void objectToString() {
			final String anonymousString = "An anonymous string from a class";

			Object o = new Object() {

				@Override
				public String toString() {
					return anonymousString;
				}

			};
			String result = PioneerUtils.nullSafeToString(o);

			assertThat(result).isEqualTo(anonymousString);
		}

		@Test
		@DisplayName("returns the number as a String for a number")
		void primitiveToString() {
			String result = PioneerUtils.nullSafeToString(12);

			assertThat(result).isEqualTo("12");
		}

		@Test
		@DisplayName("returns the String 'true' for true")
		void booleanTrueToString() {
			String result = PioneerUtils.nullSafeToString(true);

			assertThat(result).isEqualTo("true");
		}

		@Test
		@DisplayName("returns the String 'false' for false")
		void booleanFalseToString() {
			String result = PioneerUtils.nullSafeToString(false);

			assertThat(result).isEqualTo("false");
		}

		@Nested
		@DisplayName("when given an array type")
		class NullSafeToStringArrayTests {

			@Test
			@DisplayName("returns the elements of the array between [] for Object[]")
			void deepToStringForObjectArray() {
				String[] input = { "A", "B", "C", "D" };
				String result = PioneerUtils.nullSafeToString(input);

				assertThat(result).isEqualTo("[A, B, C, D]");
			}

			@Test
			@DisplayName("returns the elements of the array between appropriate amounts of [] for any depth Object[]")
			void deepToStringForAnyDepthObjectArray() {
				String[][][] input = { { { "A", "B" }, { "C" } }, { { "D" }, { "E", "F" } } };
				String result = PioneerUtils.nullSafeToString(input);

				assertThat(result).isEqualTo("[[[A, B], [C]], [[D], [E, F]]]");
			}

			@Test
			@DisplayName("returns the elements of the array between [] for boolean[]")
			void testBooleans() {
				boolean[] input = { true, false, false };
				String result = PioneerUtils.nullSafeToString(input);

				assertThat(result).isEqualTo("[true, false, false]");
			}

			@Test
			@DisplayName("returns the elements of the array between [] for byte[]")
			void testBytes() {
				byte[] input = { 124, 73, 127 };
				String result = PioneerUtils.nullSafeToString(input);

				assertThat(result).isEqualTo("[124, 73, 127]");
			}

			@Test
			@DisplayName("returns the elements of the array between [] for char[]")
			void testChars() {
				char[] input = { '1', 'b', '$' };
				String result = PioneerUtils.nullSafeToString(input);

				assertThat(result).isEqualTo("[1, b, $]");
			}

			@Test
			@DisplayName("returns the elements of the array between [] for int[]")
			void testInts() {
				int[] input = { 1111111111, 1212, 45 };
				String result = PioneerUtils.nullSafeToString(input);

				assertThat(result).isEqualTo("[1111111111, 1212, 45]");
			}

			@Test
			@DisplayName("returns the elements of the array between [] for short[]")
			void testShorts() {
				short[] input = { 14114, 5656, 42 };
				String result = PioneerUtils.nullSafeToString(input);

				assertThat(result).isEqualTo("[14114, 5656, 42]");
			}

			@Test
			@DisplayName("returns the elements of the array between [] for long[]")
			void testLongs() {
				long[] input = { 123456789L, 345345L, 42L };
				String result = PioneerUtils.nullSafeToString(input);

				assertThat(result).isEqualTo("[123456789, 345345, 42]");
			}

			@Test
			@DisplayName("returns the elements of the array between [] for double[]")
			void testDoubles() {
				double[] input = { 12.23, 34.56, 78.9 };
				String result = PioneerUtils.nullSafeToString(input);

				assertThat(result).isEqualTo("[12.23, 34.56, 78.9]");
			}

			@Test
			@DisplayName("returns the elements of the array between [] for float[]")
			void testFloats() {
				float[] input = { 12.23f, 34.56f, 78.9f };
				String result = PioneerUtils.nullSafeToString(input);

				assertThat(result).isEqualTo("[12.23, 34.56, 78.9]");
			}

		}

	}

	static class MethodFinderTestCases {

		@SuppressWarnings("unused")
		void baseMethod() {
		}

		static class MethodFinderTestCasesChild {

			static class MethodFinderTestCasesGrandChild {

				@SuppressWarnings("unused")
				void grandchildrenMethod() {
				}

			}

		}

	}

}
