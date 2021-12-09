/*
 * Copyright 2016-2021 the original author or authors.
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

/**
 * An iterator for numerical ranges, used as the backing logic for {@link RangeSourceArgumentsProvider}.
 * @param <N> The numerical type used by the range.
 */
abstract class Range<N extends Number & Comparable<N>> implements Iterator<N> {

	private N from;
	private N to;
	private N step;
	private boolean closed;
	private N current;
	private int sign;
	private N zero;

	Range(N from, N to, N step, boolean closed, N zero) {
		this.from = from;
		this.to = to;
		this.step = step;
		this.closed = closed;
		this.zero = zero;
		current = null;
		sign = step.compareTo(getZero());
	}

	/**
	 * Asserts the range is valid.
	 * @throws IllegalArgumentException if the range is not valid
	 */
	void validate() {
		if (step.equals(getZero())) {
			throw new IllegalArgumentException("Illegal range. The step cannot be zero.");
		}

		if (!closed && from.equals(to)) {
			throw new IllegalArgumentException("Illegal range. Equal from and to will produce an empty range.");
		}

		boolean fromNotEqualsTo = (from.compareTo(to) != 0);

		if ((isValidDescending()) && (!closed || fromNotEqualsTo)) {
			String message = String
					.format("Illegal range. There's no way to get from %s to %s with a step of %s.", from, to, step);
			throw new IllegalArgumentException(message);
		}
	}

	boolean isValidDescending() {
		boolean fromIsLessThanTo = (from.compareTo(to) < 0);
		boolean stepIsLessThanZero = (sign < 0);

		return fromIsLessThanTo == stepIsLessThanZero;
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
	private N getZero() {
		return zero;
	}

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
