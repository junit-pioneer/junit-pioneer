/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.converter;

import static java.lang.String.format;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.TypedArgumentConverter;
import org.junit.jupiter.params.support.AnnotationConsumer;

class ByteArrayArgumentConverter extends TypedArgumentConverter<Number, byte[]>
		implements AnnotationConsumer<ByteArrayConversion> {

	private ByteOrder order;

	public ByteArrayArgumentConverter() {
		super(Number.class, byte[].class);
	}

	@Override
	public void accept(ByteArrayConversion annotation) {
		this.order = getByteOrder(annotation);
	}

	@Override
	protected byte[] convert(Number source) throws ArgumentConversionException {
		if (source instanceof Byte) {
			var bytes = (byte) source;
			return ByteBuffer.allocate(Byte.BYTES).order(order).put(bytes).array();
		} else if (source instanceof Short) {
			var bytes = (short) source;
			return ByteBuffer.allocate(Short.BYTES).order(order).putShort(bytes).array();
		} else if (source instanceof Integer) {
			var bytes = (int) source;
			return ByteBuffer.allocate(Integer.BYTES).order(order).putInt(bytes).array();
		} else if (source instanceof Long) {
			var bytes = (long) source;
			return ByteBuffer.allocate(Long.BYTES).order(order).putLong(bytes).array();
		}
		throw new ArgumentConversionException(format("Unsupported parameter type: %s", source.getClass()));
	}

	private ByteOrder getByteOrder(ByteArrayConversion annotation) {
		switch (annotation.byteOrder()) {
			case BIG_ENDIAN:
				return ByteOrder.BIG_ENDIAN;
			case LITTLE_ENDIAN:
				return ByteOrder.LITTLE_ENDIAN;
		}
		throw new IllegalStateException(
			format("Unexpected byte order value in annotation: %s", annotation.byteOrder()));
	}

}
