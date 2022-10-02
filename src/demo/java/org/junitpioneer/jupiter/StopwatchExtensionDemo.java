/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import org.junit.jupiter.api.Test;

public class StopwatchExtensionDemo {

	// tag::method[]
	@Test
	@Stopwatch
	void test() {
		// execution time will be reported
	}
	// end::method[]

	// tag::class[]
	@Stopwatch
	class TestCases {

		@Test
		void test_1() {
			// execution time will be reported
		}

		@Test
		void test_s() {
			// execution time will be reported
		}

	}
	// end::class[]

}
