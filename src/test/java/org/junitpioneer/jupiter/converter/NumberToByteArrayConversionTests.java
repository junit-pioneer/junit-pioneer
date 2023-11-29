/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.converter.NumberToByteArrayConversion.ByteOrder;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;
import org.junitpioneer.testkit.assertion.PioneerAssert;

public class NumberToByteArrayConversionTests {

	@ParameterizedTest
	@ValueSource(ints = { Integer.MIN_VALUE, 0, 0xFF, 0xFFFF, 0xFFFFFF, Integer.MAX_VALUE })
	void testInts(@NumberToByteArrayConversion byte[] byteArray) {
		assertThat(byteArray).hasSize(4);
	}

	@ParameterizedTest
	@ValueSource(bytes = { Byte.MIN_VALUE, 0, Byte.MAX_VALUE })
	void testBytes(@NumberToByteArrayConversion byte[] byteArray) {
		assertThat(byteArray).hasSize(1);
	}

	@ParameterizedTest
	@ValueSource(shorts = { Short.MIN_VALUE, 0, Short.MAX_VALUE })
	void testShorts(@NumberToByteArrayConversion byte[] byteArray) {
		assertThat(byteArray).hasSize(2);
	}

	@ParameterizedTest
	@ValueSource(longs = { Long.MIN_VALUE, 0, Long.MAX_VALUE })
	void testLongs(@NumberToByteArrayConversion byte[] byteArray) {
		assertThat(byteArray).hasSize(8);
	}

	@ParameterizedTest
	@ValueSource(ints = { (256 * 256 * 6 + 256 * 36 + 66) })
	void testBigEndianOrder(@NumberToByteArrayConversion(order = ByteOrder.BIG_ENDIAN) byte[] byteArray) {
		assertThat(byteArray).hasSize(4).containsExactly(0, 6, 36, 66);
	}

	@ParameterizedTest
	@ValueSource(ints = { (256 * 256 * 6 + 256 * 36 + 66) })
	void testLittleEndianOrder(@NumberToByteArrayConversion(order = ByteOrder.LITTLE_ENDIAN) byte[] byteArray) {
		assertThat(byteArray).hasSize(4).containsExactly(66, 36, 6, 0);
	}

	@ParameterizedTest
	@ValueSource(doubles = { Double.MIN_VALUE, 0.0, Double.MAX_VALUE })
	void testDoubles(@NumberToByteArrayConversion byte[] byteArray) {
		assertThat(byteArray).hasSize(8);
	}

	@ParameterizedTest
	@ValueSource(floats = { Float.MIN_VALUE, 0.0f, Float.MAX_VALUE })
	void testFloats(@NumberToByteArrayConversion byte[] byteArray) {
		assertThat(byteArray).hasSize(4);
	}

	@Test
	void throwsException() {
		ExecutionResults result = PioneerTestKit
				.executeTestMethodWithParameterTypes(UnsupportedTestCases.class, "throwsException", byte[].class);

		PioneerAssert
				.assertThat(result)
				.hasSingleFailedTest()
				.withExceptionInstanceOf(ParameterResolutionException.class)
				.hasMessageContaining("Unsupported parameter type");
	}

	static class UnsupportedTestCases {

		@ParameterizedTest
		@ArgumentsSource(BigDecimalProvider.class)
		void throwsException(@NumberToByteArrayConversion byte[] byteArray) {
		}

	}

	static class BigDecimalProvider implements ArgumentsProvider {

		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(Arguments.of(BigDecimal.ONE));
		}

	}

}
