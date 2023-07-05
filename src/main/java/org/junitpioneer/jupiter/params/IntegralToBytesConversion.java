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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.params.converter.ConvertWith;

/**
 * Annotation to convert an integral type (byte, short, int, long) to a byte array.
 * The converter uses {@link java.nio.ByteBuffer} under the hood.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@ConvertWith(IntegralToBytesConverter.class)
public @interface IntegralToBytesConversion {

	/**
	 * The byte order to use during the conversion.
	 * Note that this is NOT {@link java.nio.ByteOrder}
	 * because that is not an enum (and can not be used in an annotation).
	 *
	 * @return the byte order to use, either {@code BIG_ENDIAN} or {@code LITTLE_ENDIAN}.
	 */
	ByteOrder byteOrder() default ByteOrder.BIG_ENDIAN;

}
