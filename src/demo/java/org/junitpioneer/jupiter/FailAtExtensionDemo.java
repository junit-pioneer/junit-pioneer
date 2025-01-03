/*
 * Copyright 2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@Disabled
public class FailAtExtensionDemo {

	// tag::fail_at_simple[]
	@Test
	@FailAt(date = "2050-01-01")
	void test() {
		// Test will fail as soon as 1st of January 2050 is reached.
	}
	// end::fail_at_simple[]

	// tag::fail_at_with_reason[]
	@Test
	@FailAt(reason = "We are not allowed anymore", date = "2050-01-01")
	void testWithReason() {
		// Test will fail with the given reason as soon as 1st of January 2050 is reached.
	}
	// end::fail_at_with_reason[]

	@Nested
	// tag::fail_at_at_class_level[]
	@FailAt(date = "2050-01-01")
	class TestClass {

		@Test
		void test() {
			// Test will fail as soon as 1st of January 2050 is reached.
		}

	}
	// end::fail_at_at_class_level[]

}
