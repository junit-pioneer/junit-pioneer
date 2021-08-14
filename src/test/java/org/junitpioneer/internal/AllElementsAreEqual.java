/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.internal;

import java.util.List;

import org.assertj.core.api.Condition;

public final class AllElementsAreEqual extends Condition<List<?>> {

	private static final AllElementsAreEqual INSTANCE = new AllElementsAreEqual();

	private AllElementsAreEqual() {
		super(elements -> elements.stream().skip(1).allMatch(e -> e.equals(elements.get(0))),
			"is a list where all the elements are equal");
	}

	public static AllElementsAreEqual allElementsAreEqual() {
		return AllElementsAreEqual.INSTANCE;
	}

}
