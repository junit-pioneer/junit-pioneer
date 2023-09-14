/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.params;

class DoubleRange extends Range<Double> {

	public DoubleRange(DoubleRangeSource source) {
		super(source.from(), source.to(), source.step(), source.closed(), 0.0D);
	}

	@Override
	public Double nextValue() {
		return getCurrent() + getStep();
	}

}
