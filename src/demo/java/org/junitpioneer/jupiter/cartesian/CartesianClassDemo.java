/*
 * Copyright 2016-2026 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.cartesian;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.params.IntRangeSource;

// tag::cartesian_class[]
@CartesianClass(name = "[{index}] size={0}, mode={1}")
class CartesianClassDemo {

	private final int size;
	private final Mode mode;

	CartesianClassDemo(@IntRangeSource(from = 1, to = 3) int size, @CartesianTest.Enum Mode mode) {
		this.size = size;
		this.mode = mode;
	}

	@Test
	void sizeIsPositive() {
		assertThat(size).isPositive();
	}

	@Test
	void modeIsAvailable() {
		assertThat(mode).isIn(Mode.FAST, Mode.SAFE);
	}

	enum Mode {
		FAST, SAFE
	}

}
// end::cartesian_class[]
