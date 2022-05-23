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

import org.junit.jupiter.params.ParameterizedTest;

public class RangeSourcesDemo {

	// tag::rangesources_int_valid_digit[]
	@ParameterizedTest
	@IntRangeSource(from = 0, to = 10)
	// called 10 times with `digit` = 0, 1, 2, 3, 4, 5, 6, 7, 8, 9
	void validDigit(int digit) {
		System.out.println(digit + " is a valid digit");
	}
	// end::rangesources_int_valid_digit[]

	// tag::rangesources_double_with_step[]
	@ParameterizedTest
	@DoubleRangeSource(from = -0.1, to = -10, step = -0.1)
	void howColdIsIt(double d) {
		System.out.println(d + " °C is cold");
		System.out.println(d + " °F is REALY cold");
		System.out.println(d + " K is too cold to be true");
	}
	// end::rangesources_double_with_step[]

	@ParameterizedTest
	@LongRangeSource(from = 0L, to = 0L, closed = true)
	void legalRrange(long arg) {
		// But this is fine
		assertThat(arg).isEqualTo(0L);
	}
	// end::rangesources_ranges[]

	class TheseTestsWillFailIntentionally {

		// tag::rangesources_ranges[]
		@ParameterizedTest
		@ByteRangeSource(from = 0, to = 0)
		void illegalRange(byte arg) {
			// this will fail with an IllegalArgumentException
			// since the range will be empty
		}

	}

}
