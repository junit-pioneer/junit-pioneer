/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import java.util.Arrays;

import org.opentest4j.TestAbortedException;

public final class Disable {

	private String reason;
	private Object[] params;

	private Disable(Object[] params) {
		this.params = params;
	}

	public static Disable when(Object... params) {
		return new Disable(params);
	}

	public Disable contains(Object value) {
		if (Arrays.asList(params).contains(value)) {
			abortTest(value);
		}
		return this;
	}

	public Disable is(Object... values) {
		if (values.length != params.length) {
			throw new IllegalArgumentException("Incorrect number of arguments for disabling tests, expected: "
					+ params.length + ", but got: " + values.length);
		}
		if (Arrays.equals(params, values)) {
			abortTest(values);
		}
		return this;
	}

	public void becauseOf(String reason) {
		this.reason = reason;
	}

	private void abortTest(Object... values) {
		if (reason != null) {
			throw new TestAbortedException(
				"Test aborted for arguments " + Arrays.toString(values) + ", see: " + reason);
		}
		throw new TestAbortedException("Test aborted for arguments " + Arrays.toString(values));
	}

}
