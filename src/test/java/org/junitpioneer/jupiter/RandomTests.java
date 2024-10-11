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

import org.junit.jupiter.api.Test;

public class RandomTests {

	@Test
	void test(@Random Wrapper wrapper) {
		System.out.println(wrapper);
	}

	public static class Wrapper {

		private final int i;
		private final boolean bool;

		public Wrapper(int i, boolean bool) {
			this.i = i;
			this.bool = bool;
		}

		@Override
		public String toString() {
			return "Wrapper{" + "i=" + i + ", bool=" + bool + '}';
		}

	}

}
