/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ExpectedToFailExtensionDemo {

	// tag::expected_to_fail[]
	@Test
	@ExpectedToFail(onExceptions = Exception.class)
	void test() {
		int actual = brokenMethod();
		assertEquals(10, actual);
	}
	// end::expected_to_fail[]

	// tag::expected_to_fail_message[]
	@Test
	@ExpectedToFail("Implementation bug in brokenMethod()")
	void doSomething() {
		int actual = brokenMethod();
		assertEquals(10, actual);
	}

	private int brokenMethod() {
		return 0;
	}
	// end::expected_to_fail_message[]

	// tag::expected_to_fail_onexception[]
	@Test
	@ExpectedToFail(onExceptions = UnsupportedOperationException.class)
	void testProductionFeature() {
		int actual = productionFeature();
		assertEquals(10, actual);
	}

	private int productionFeature() {
		throw new UnsupportedOperationException("productionFeature() is not yet implemented");
	}
	// end::expected_to_fail_onexception[]

}
