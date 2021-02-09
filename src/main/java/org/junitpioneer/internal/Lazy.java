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

import java.util.function.Supplier;

/**
 * Pioneer-internal, incomplete and thread-unsafe implementation of the lazy monad.
 * DO NOT USE THIS CLASS - IT MAY CHANGE SIGNIFICANTLY IN ANY MINOR UPDATE.
 */
public class Lazy<ELEMENT> {

	private final Supplier<ELEMENT> supplier;

	private boolean supplied;
	private ELEMENT element;

	private Lazy(Supplier<ELEMENT> supplier) {
		this.supplier = supplier;
		this.supplied = false;
	}

	public static <ELEMENT> Lazy<ELEMENT> from(Supplier<ELEMENT> supplier) {
		return new Lazy<>(supplier);
	}

	public ELEMENT get() {
		if (!supplied) {
			element = supplier.get();
			supplied = true;
		}
		return element;
	}

}
