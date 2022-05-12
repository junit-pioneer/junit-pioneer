/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.params;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

/**
 * Tests for the {@link RangeSourceArgumentsProvider}.
 */
class RangeSourceArgumentsProviderTests {

	private Number[] expectedValues;

	@BeforeEach
	void populateValues() {
		expectedValues = Stream
				.of(IntStream.range(0, 10).mapToObj(i -> (byte) i),
					IntStream.rangeClosed(-7, -3).mapToObj(i -> (byte) i),
					IntStream.range(130, 136).mapToObj(i -> (short) i),
					IntStream.rangeClosed(-144, -140).filter(i -> i % 2 == 0).mapToObj(i -> (short) i),
					IntStream.range(40_000, 40_005).boxed(),
					IntStream.rangeClosed(-42_000, -40_000).filter(i -> i % 2000 == 0).boxed(),
					LongStream.range(6_000_000_000L, 6_000_000_003L).boxed(),
					LongStream.rangeClosed(-6_000_000_400L, -6_000_000_000L).filter(l -> l % 100L == 0L).boxed(),
					IntStream.range(0, 3).mapToObj(i -> i + 2.2F), IntStream.rangeClosed(3, 6).mapToObj(i -> i * -0.1F),
					IntStream.range(0, 2).mapToObj(i -> i + 8.4),
					IntStream.rangeClosed(-3, -2).mapToObj(i -> (double) i), Stream.of(123), Stream.of((byte) 120),
					Stream.of((byte) -120))
				.flatMap(Function.identity())
				.toArray(Number[]::new);
	}

	@Test
	void assertAllValuesSupplied() {
		ExecutionResults results = PioneerTestKit.executeTestClass(RangeTestCases.class);

		List<Number> actualValues = results
				.dynamicallyRegisteredEvents()
				.map(e -> e.getTestDescriptor().getDisplayName())
				.map(RangeSourceArgumentsProviderTests::displayNameToNumber)
				.collect(Collectors.toList());

		assertThat(actualValues).containsExactlyInAnyOrder(expectedValues);
	}

	private static Number displayNameToNumber(String displayName) {
		String[] split = displayName.split(" ");
		String type = split[0];
		String value = split[1];

		try {
			Class<?> valueClass = Class.forName("java.lang." + type);
			Method parseMethod = valueClass.getMethod("valueOf", String.class);
			return (Number) parseMethod.invoke(null, value);
		}
		catch (Exception ignore) {
			return null;
		}
	}

	@Nested
	class RangeTestCases {

		@ParameterizedTest(name = "Byte {0}")
		@ByteRangeSource(from = 0, to = 10)
		void ascendingByte(byte param) {
		}

		@ParameterizedTest(name = "Byte {0}")
		@ByteRangeSource(from = -3, to = -7, step = -1, closed = true)
		void descendingByte(byte param) {
		}

		@ParameterizedTest(name = "Short {0}")
		@ShortRangeSource(from = 130, to = 136)
		void ascendingShort(short param) {
		}

		@ParameterizedTest(name = "Short {0}")
		@ShortRangeSource(from = -140, to = -144, step = -2, closed = true)
		void descendingShort(short param) {
		}

		@ParameterizedTest(name = "Integer {0}")
		@IntRangeSource(from = 40_000, to = 40_005)
		void ascendingInt(int param) {
		}

		@ParameterizedTest(name = "Integer {0}")
		@IntRangeSource(from = -40_000, to = -42_000, step = -2000, closed = true)
		void descendingInt(int param) {
		}

		@ParameterizedTest(name = "Long {0}")
		@LongRangeSource(from = 6_000_000_000L, to = 6_000_000_003L)
		void ascendingLong(long param) {
		}

		@ParameterizedTest(name = "Long {0}")
		@LongRangeSource(from = -6_000_000_000L, to = -6_000_000_400L, step = -100, closed = true)
		void descendingLong(long param) {
		}

		@ParameterizedTest(name = "Float {0}")
		@FloatRangeSource(from = 2.2F, to = 5.2F)
		void ascendingFloat(float param) {
		}

		@ParameterizedTest(name = "Float {0}")
		@FloatRangeSource(from = -0.3F, to = -0.6F, step = -0.1F, closed = true)
		void descendingFloat(float param) {
		}

		@ParameterizedTest(name = "Double {0}")
		@DoubleRangeSource(from = 8.4, to = 10.4)
		void ascendingDouble(double param) {
		}

		@ParameterizedTest(name = "Double {0}")
		@DoubleRangeSource(from = -2.0, to = -3.0, step = -1, closed = true)
		void descendingDouble(double param) {
		}

		@ParameterizedTest(name = "Integer {0}")
		@IntRangeSource(from = 123, to = 123, closed = true)
		void emptyClosedRange(int param) {
		}

		@ParameterizedTest(name = "Byte {0}")
		@ByteRangeSource(from = 120, to = 125, step = 10)
		void overflowProtection(byte param) {
		}

		@ParameterizedTest(name = "Byte {0}")
		@ByteRangeSource(from = -120, to = -125, step = -10)
		void underflowProtection(byte param) {
		}

	}

	@Nested
	class InvalidRangeTests {

		@Test
		void twoAnnotations() {
			ExecutionResults results = PioneerTestKit.executeTestMethod(InvalidRangeTestCases.class, "twoAnnotations");

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(IllegalArgumentException.class)
					.hasMessageContainingAll("Expected exactly one annotation to provide an ArgumentSource, found 2.");
		}

		@Test
		void zeroStep() {
			ExecutionResults results = PioneerTestKit.executeTestMethod(InvalidRangeTestCases.class, "zeroStep");

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(IllegalArgumentException.class)
					.hasMessageContainingAll("Illegal range. The step cannot be zero.");
		}

		@Test
		void illegalStep() {
			ExecutionResults results = PioneerTestKit.executeTestMethod(InvalidRangeTestCases.class, "illegalStep");

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(IllegalArgumentException.class)
					.hasMessageContainingAll("Illegal range. There's no way to get from 10 to 0 with a step of 1.");
		}

		@Test
		void emptyRange() {
			ExecutionResults results = PioneerTestKit.executeTestMethod(InvalidRangeTestCases.class, "emptyRange");

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(IllegalArgumentException.class)
					.hasMessageContainingAll("Illegal range. Equal from and to will produce an empty range.");
		}

	}

	static class InvalidRangeTestCases {

		@IntRangeSource(from = 1, to = 2)
		@LongRangeSource(from = 1L, to = 2L)
		@ParameterizedTest
		void twoAnnotations() {
		}

		@IntRangeSource(from = 1, to = 2, step = 0)
		@ParameterizedTest
		void zeroStep() {
		}

		@IntRangeSource(from = 10, to = 0, step = 1)
		@ParameterizedTest
		void illegalStep() {
		}

		@IntRangeSource(from = 7, to = 7, step = 1)
		@ParameterizedTest
		void emptyRange() {
		}

	}

}
