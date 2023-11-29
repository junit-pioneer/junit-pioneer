/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.vintage;

import java.util.List;

public class VintageTestDemo {

	// tag::vintage_test_indexoutofbound_exception[]
	@Test(expected = IndexOutOfBoundsException.class)
	public void outOfBounds_passes() {
		List.of().get(1);
	}
	// end::vintage_test_indexoutofbound_exception[]

	// tag::vintage_test_runtime_exception[]
	@Test(expected = RuntimeException.class)
	public void outOfBounds_passes_too() {
		List.of().get(1);
	}
	// end::vintage_test_runtime_exception[]

	class TheseTestsWillFailIntentionally {

		// tag::vintage_test_iae_exception[]
		@Test(expected = IllegalArgumentException.class)
		public void outOfBounds_fails() {
			List.of().get(1);
		}
		// end::vintage_test_iae_exception[]

		// tag::vintage_test_timeout[]
		@Test(timeout = 100)
		public void slow_fail() throws InterruptedException {
			Thread.sleep(1_000);
		}
		// end::vintage_test_timeout[]

		// tag::vintage_test_timeout_loop[]
		@Test(timeout = 100)
		public void indefinitely() {
			while (true)
				;
		}
		// end::vintage_test_timeout_loop[]

	}

}
