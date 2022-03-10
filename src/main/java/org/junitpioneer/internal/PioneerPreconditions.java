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

import java.util.Collection;
import java.util.function.Supplier;

import org.junit.platform.commons.PreconditionViolationException;

/**
 * Pioneer-internal utility class to handle preconditions.
 * DO NOT USE THIS CLASS - IT MAY CHANGE SIGNIFICANTLY IN ANY MINOR UPDATE.
 */
public class PioneerPreconditions {

	private PioneerPreconditions() {
		// private constructor to prevent instantiation of utility class
	}

	/**
	 * Assert that the supplied string is not blank.
	 * @param str the string to check
	 * @param messageSupplier the precondition violation message supplier
	 * @return the supplied string
	 */
	public static String notBlank(String str, Supplier<String> messageSupplier) {
		if (str == null || str.trim().isEmpty()) {
			throw new PreconditionViolationException(messageSupplier.get());
		}

		return str;
	}

	/**
	 * Assert that the supplied object is not null.
	 * @param object the object to check
	 * @param messageSupplier the precondition violation message supplier
	 * @return the supplied object
	 */
	public static <T> T notNull(T object, Supplier<String> messageSupplier) {
		if (object == null) {
			throw new PreconditionViolationException(messageSupplier.get());
		}
		return object;
	}

	/**
	 * Assert that the supplied collection is not empty.
	 * @param collection the collection to check
	 * @param messageSupplier the precondition violation message supplier
	 * @return the supplied string
	 */
	public static <T extends Collection<?>> T notEmpty(T collection, Supplier<String> messageSupplier) {
		if (collection == null || collection.isEmpty()) {
			throw new PreconditionViolationException(messageSupplier.get());
		}
		return collection;
	}

	/**
	 * Assert that the supplied collection is not empty.
	 * @param collection the collection to check
	 * @param message the precondition violation message supplier
	 * @return the supplied string
	 */
	public static <T extends Collection<?>> T notEmpty(T collection, String message) {
		if (collection == null || collection.isEmpty()) {
			throw new PreconditionViolationException(message);
		}
		return collection;
	}

}
