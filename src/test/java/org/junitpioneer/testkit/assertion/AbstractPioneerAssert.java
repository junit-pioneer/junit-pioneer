/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.testkit.assertion;

import org.assertj.core.api.AbstractAssert;

public abstract class AbstractPioneerAssert<SELF extends AbstractAssert<SELF, ACTUAL>, ACTUAL>
		extends AbstractAssert<SELF, ACTUAL> {

	protected final int expected;

	protected AbstractPioneerAssert(ACTUAL actual, Class<?> self, int expected) {
		super(actual, self);
		this.expected = expected;
	}

}
