/*
 * Copyright 2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.random;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.Random;

import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class RandomTests {

	@Test
	void testPrimitive(@Random(seed = 11) @Min(100) @Max(100) int primitive) {
		System.out.println(primitive);
	}

	@Test
	void test(@Random Simple simple) {
		System.out.println(simple);
	}

	@Test
	void test2(@Random Complex complex) {
		System.out.println(complex);
	}

	public static class Simple {

		@Min(1)
		@Max(1000)
		private final int i;

		@AssertFalse
		private final boolean bool;

		public Simple(int i, boolean bool) {
			this.i = i;
			this.bool = bool;
		}

		@Override
		public String toString() {
			return "Simple{" + "i=" + i + ", bool=" + bool + '}';
		}

	}

	public static class Complex {

		private final Simple simple;
		private final double d;

		public Complex(Simple simple, double d) {
			this.simple = simple;
			this.d = d;
		}

		@Override
		public String toString() {
			return "Complex{" + "simple=" + simple + ", d=" + d + '}';
		}

	}

}
