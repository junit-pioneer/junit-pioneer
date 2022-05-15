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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

public class DisableIfTestFailsExtensionDemo {

	// tag::disable_if_test_fails[]
	@DisableIfTestFails
	// this annotation ensures that tests are run in the order of
	// the numbers passed to the `@Order` annotation below
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class Tests {

		@Test
		@Order(1)
		void test1() {
		}

		@Test
		@Order(2)
		void test2() {
			fail();
		}

		@Test
		@Order(3)
		void test3() {
		}

	}
	// end::disable_if_test_fails[]

	// tag::disable_if_test_not_on_assertions[]
	@DisableIfTestFails(onAssertion = false)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	static class ThreeTestsWithSecondFailingWithUnconfiguredAssertionTestCase {

		@Test
		@Order(1)
		void test1() {
		}

		@Test
		@Order(2)
		void test2() {
			// fails test with assertion
			assertTrue(false);
		}

		@Test
		@Order(3)
		void test3() {
		}

	}
	// end::disable_if_test_not_on_assertions[]

	// tag::disable_if_test_with_given_exception[]
	@DisableIfTestFails(with = IOException.class)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	static class ThreeTestsWithSecondThrowingConfiguredExceptionTestCase {

		@Test
		@Order(1)
		void test1() {
		}

		@Test
		@Order(2)
		void test2() throws InterruptedException {
			throw new InterruptedException();
		}

		@Test
		@Order(3)
		void test3() throws IOException {
			throw new IOException();
		}

		@Test
		@Order(4)
		void test4() {
		}

	}

	// end::disable_if_test_with_given_exception[]
}
