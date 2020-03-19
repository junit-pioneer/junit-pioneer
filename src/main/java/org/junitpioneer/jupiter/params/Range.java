/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.params;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;

/**
 * An iterator for numerical ranges, used as the backing logic for {@link RangeSourceProvider}.
 * @param <N> The numerical type used by the range.
 */
abstract class Range<N extends Number & Comparable<N>> implements Iterator<N> {

	private N from;
	private N to;
	private N step;
	private boolean closed;
	private N current;
	private int sign;

	Range(N from, N to, N step, boolean closed) {
		this.from = from;
		this.to = to;
		this.step = step;
		this.closed = closed;
		current = null;
		sign = step.compareTo(getZero());
	}

	/**
	 * Asserts the range is valid.
	 * @throws PreconditionViolationException if the range is not valid
	 */
	void validate() throws PreconditionViolationException {
		Preconditions.condition(!step.equals(getZero()), "Illegal range. The step cannot be zero.");

		Preconditions
				.condition(closed || !from.equals(to), "Illegal range. Equal from and to will produce an empty range.");

		int cmp = from.compareTo(to);
		Preconditions
				.condition((cmp < 0 != sign < 0) || (closed && cmp == 0),
					() -> String
							.format("Illegal range. There's no way to get from %s to %s with a step of %s.", from, to,
								step));
	}

	N getStep() {
		return step;
	}

	N getCurrent() {
		return current;
	}

	/**
	 * The next value in the range. Calling {@link #next()} will return this value and advance the iterator to it.
	 */
	abstract N nextValue();

	private N getNextValue() {
		if (current == null) {
			return from;
		}
		return nextValue();
	}

	/**
	 * The value of the no-op "zero", illegal step in terms of N
	 */
	abstract N getZero();

	@Override
	public boolean hasNext() {
		if (current == null) {
			return true;
		}
		N nextValue = getNextValue();
		int cmp = nextValue.compareTo(to);
		int overflowCheck = nextValue.compareTo(current);
		return ((cmp < 0 != sign < 0) && (overflowCheck < 0 == sign < 0)) || (closed && cmp == 0);
	}

	@Override
	public N next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		current = getNextValue();
		return current;
	}

}
