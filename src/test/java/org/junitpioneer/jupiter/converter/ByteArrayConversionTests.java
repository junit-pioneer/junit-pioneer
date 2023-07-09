/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.converter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ByteArrayConversionTests {

	@ParameterizedTest
	@ValueSource(ints = { Integer.MIN_VALUE, 0, 0xFF, 0xFFFF, 0xFFFFFF, Integer.MAX_VALUE })
	void test(@ByteArrayConversion byte[] byteArray) {
		assertThat(byteArray).hasSize(4);
	}

	@ParameterizedTest
	@ValueSource(bytes = { Byte.MIN_VALUE, 0, Byte.MAX_VALUE })
	void testBytes(@ByteArrayConversion byte[] byteArray) {
		assertThat(byteArray).hasSize(1);
	}

	@ParameterizedTest
	@ValueSource(shorts = { Short.MIN_VALUE, 0, Short.MAX_VALUE })
	void testShorts(@ByteArrayConversion byte[] byteArray) {
		assertThat(byteArray).hasSize(2);
	}

	@ParameterizedTest
	@ValueSource(longs = { Long.MIN_VALUE, 0, Long.MAX_VALUE })
	void testLongs(@ByteArrayConversion byte[] byteArray) {
		assertThat(byteArray).hasSize(8);
	}

	@ParameterizedTest
	@ValueSource(ints = { (256 * 256 * 6 + 256 * 36 + 66) })
	void testBigEndianOrder(@ByteArrayConversion(byteOrder = ByteOrder.BIG_ENDIAN) byte[] byteArray) {
		assertThat(byteArray).hasSize(4).containsExactly(0, 6, 36, 66);
	}

	@ParameterizedTest
	@ValueSource(ints = { (256 * 256 * 6 + 256 * 36 + 66) })
	void testLittleEndianOrder(@ByteArrayConversion(byteOrder = ByteOrder.LITTLE_ENDIAN) byte[] byteArray) {
		assertThat(byteArray).hasSize(4).containsExactly(66, 36, 6, 0);
	}

}
