/*
 * Copyright 2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.random;

import java.util.Random;

/**
 * A partial backport of {@code RandomSupport} from Java 17,
 * so we can use that code in Java 11.
 */
public class PioneerRandomUtils {

	private PioneerRandomUtils() {
	}

	public static double boundedNextDouble(Random rng, double origin, double bound) {
		double r = rng.nextDouble();
		if (origin < bound) {
			if (bound - origin < Double.POSITIVE_INFINITY) {
				r = r * (bound - origin) + origin;
			} else {
				/* avoids overflow at the cost of 3 more multiplications */
				double halfOrigin = 0.5 * origin;
				r = (r * (0.5 * bound - halfOrigin) + halfOrigin) * 2.0;
			}
			if (r >= bound) // may need to correct a rounding problem
				r = Math.nextDown(bound);
		}
		return r;
	}

	public static float boundedNextFloat(Random rng, float origin, float bound) {
		float r = rng.nextFloat();
		if (origin < bound) {
			if (bound - origin < Float.POSITIVE_INFINITY) {
				r = r * (bound - origin) + origin;
			} else {
				/* avoids overflow at the cost of 3 more multiplications */
				float halfOrigin = 0.5f * origin;
				r = (r * (0.5f * bound - halfOrigin) + halfOrigin) * 2.0f;
			}
			if (r >= bound) // may need to correct a rounding problem
				r = Math.nextDown(bound);
		}
		return r;
	}

	public static int boundedNextInt(Random rng, int origin, int bound) {
		int r = rng.nextInt();
		if (origin < bound) {
			// It's not case (1).
			final int n = bound - origin;
			final int m = n - 1;
			if ((n & m) == 0) {
				// It is case (2): length of range is a power of 2.
				r = (r & m) + origin;
			} else if (n > 0) {
				// It is case (3): need to reject over-represented candidates.
				for (int u = r >>> 1; u + m - (r = u % n) < 0; u = rng.nextInt() >>> 1) //NOSONAR this is copy-pasted from Java 17 proper
					;
				r += origin;
			} else {
				// It is case (4): length of range not representable as long.
				while (r < origin || r >= bound) {
					r = rng.nextInt();
				}
			}
		}
		return r;
	}

	public static long boundedNextLong(Random rng, long origin, long bound) {
		long r = rng.nextLong();
		if (origin < bound) {
			// It's not case (1).
			final long n = bound - origin;
			final long m = n - 1;
			if ((n & m) == 0L) {
				// It is case (2): length of range is a power of 2.
				r = (r & m) + origin;
			} else if (n > 0L) {
				// It is case (3): need to reject over-represented candidates.
				/* This loop takes an unlovable form (but it works):
				   because the first candidate is already available,
				   we need a break-in-the-middle construction,
				   which is concisely but cryptically performed
				   within the while-condition of a body-less for loop. */
				for (long u = r >>> 1; // ensure nonnegative
						u + m - (r = u % n) < 0L; // rejection check //NOSONAR this is copy-pasted from Java 17 proper
						u = rng.nextLong() >>> 1) // retry
					;
				r += origin;
			} else {
				// It is case (4): length of range not representable as long.
				while (r < origin || r >= bound)
					r = rng.nextLong();
			}
		}
		return r;
	}

}
