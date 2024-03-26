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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class ExpectedToFailExtensionDemo {

	// tag::expected_to_fail[]
	@Test
	@ExpectedToFail
	void test() {
		int actual = brokenMethod();
		assertThat(actual).isEqualTo(10);
	}
	// end::expected_to_fail[]

	// tag::expected_to_fail_message[]
	@Test
	@ExpectedToFail("Implementation bug in brokenMethod()")
	void doSomething() {
		int actual = brokenMethod();
		assertThat(actual).isEqualTo(10);
	}
	// end::expected_to_fail_message[]

	private int brokenMethod() {
		return 0;
	}

	// tag::expected_to_fail_withexception[]
	@Test
	@ExpectedToFail(withExceptions = UnsupportedOperationException.class)
	void testProductionFeature() {
		int actual = productionFeature();
		assertThat(actual).isEqualTo(10);
	}

	private int productionFeature() {
		throw new UnsupportedOperationException("productionFeature() is not yet implemented");
	}
	// end::expected_to_fail_withexception[]

}
