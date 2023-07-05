/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.params;

/**
 * Utility enum because {@link java.nio.ByteOrder} is not an enum.
 */
public enum ByteOrder {

	/**
	 * Constant denoting big-endian byte order.
	 * In this order, the bytes of a multibyte value are ordered from most significant to least significant.
	 * This is a wrapper for {@code java.nio.ByteOrder.BIG_ENDIAN}.
	 */
	BIG_ENDIAN(java.nio.ByteOrder.BIG_ENDIAN),

	/**
	 * Constant denoting little-endian byte order.
	 * In this order, the bytes of a multibyte value are ordered from least significant to most significant.
	 * This is a wrapper for {@code java.nio.ByteOrder.LITTLE_ENDIAN}.
	 */
	LITTLE_ENDIAN(java.nio.ByteOrder.LITTLE_ENDIAN);

	private final java.nio.ByteOrder value;

	ByteOrder(java.nio.ByteOrder value) {
		this.value = value;
	}

	public java.nio.ByteOrder getValue() {
		return value;
	}

}
