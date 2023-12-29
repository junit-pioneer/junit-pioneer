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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ByteArrayConverterDemo {

	// tag::int_example[]
	@ParameterizedTest
	@ValueSource(ints = { 1025 })
	void intExample(@NumberToByteArrayConversion byte[] bytes) {
		assertThat(bytes).hasSize(4).containsExactly(0, 0, 4, 1);
	}
	// end::int_example[]

	// tag::long_example[]
	@ParameterizedTest
	@ValueSource(longs = { 393796333641L })
	void longExample(@NumberToByteArrayConversion byte[] bytes) {
		assertThat(bytes).hasSize(8).containsExactly(0, 0, 0, 91, 176, 23, 48, 73);
	}
	// end::long_example[]

	// tag::little_endian_order[]
	@ParameterizedTest
	@ValueSource(ints = { 1025 })
	void test(@NumberToByteArrayConversion(order = NumberToByteArrayConversion.ByteOrder.LITTLE_ENDIAN) byte[] bytes) {
		assertThat(bytes).hasSize(4).containsExactly(1, 4, 0, 0);
	}
	// end::little_endian_order[]

}
