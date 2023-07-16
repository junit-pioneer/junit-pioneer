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

public class ByteArrayConverterDemo {

	// tag::byte_array_conversion[]
	@ParameterizedTest
	@ValueSource(ints = { 13, 17, 23, 29 })
	void test(@NumberToByteArrayConversion byte[] bytes) {
		assertThat(bytes).hasSize(4);
	}
	// end::byte_array_conversion[]

}
