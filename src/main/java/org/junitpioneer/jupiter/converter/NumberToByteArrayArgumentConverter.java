/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.converter;

import static java.lang.String.format;
import static org.junitpioneer.jupiter.converter.NumberToByteArrayConversion.ByteOrder.BIG_ENDIAN;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.TypedArgumentConverter;
import org.junit.jupiter.params.support.AnnotationConsumer;

class NumberToByteArrayArgumentConverter extends TypedArgumentConverter<Number, byte[]>
		implements AnnotationConsumer<NumberToByteArrayConversion> {

	private ByteOrder order;

	public NumberToByteArrayArgumentConverter() {
		super(Number.class, byte[].class);
	}

	@Override
	public void accept(NumberToByteArrayConversion annotation) {
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
		} else if (source instanceof Double) {
			var bytes = (double) source;
			return ByteBuffer.allocate(Double.BYTES).order(order).putDouble(bytes).array();
		} else if (source instanceof Float) {
			var bytes = (float) source;
			return ByteBuffer.allocate(Float.BYTES).order(order).putFloat(bytes).array();
		}
		throw new ArgumentConversionException(format("Unsupported parameter type: %s", source.getClass()));
	}

	private ByteOrder getByteOrder(NumberToByteArrayConversion annotation) {
		if (annotation.order() == BIG_ENDIAN) {
			return ByteOrder.BIG_ENDIAN;
		} else {
			return ByteOrder.LITTLE_ENDIAN;
		}
	}

}
