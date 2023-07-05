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

import static java.lang.String.format;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.platform.commons.support.AnnotationSupport;

class IntegralToBytesConverter implements ArgumentConverter {

	public IntegralToBytesConverter() {
		// recreate default constructor to prevent compiler warning
	}

	@Override
	public byte[] convert(Object source, ParameterContext context) throws ArgumentConversionException {
		java.nio.ByteOrder byteOrder = AnnotationSupport
				.findAnnotation(context.getParameter(), IntegralToBytesConversion.class)
				.map(IntegralToBytesConversion::byteOrder)
				.map(ByteOrder::getValue)
				.orElseThrow(() -> new IllegalStateException(
					format("Could not find %s annotation on the parameter", IntegralToBytesConversion.class)));
		if (source instanceof Byte) {
			var bytes = (byte) source;
			return createByteBuffer(Byte.BYTES, byteOrder).put(bytes).array();
		} else if (source instanceof Short) {
			var bytes = (short) source;
			return createByteBuffer(Short.BYTES, byteOrder).putShort(bytes).array();
		} else if (source instanceof Integer) {
			var bytes = (int) source;
			return createByteBuffer(Integer.BYTES, byteOrder).putInt(bytes).array();
		} else if (source instanceof Long) {
			var bytes = (long) source;
			return createByteBuffer(Long.BYTES, byteOrder).putLong(bytes).array();
		}
		throw new ArgumentConversionException(format("Unsupported parameter type: %s", source.getClass()));
	}

	private static ByteBuffer createByteBuffer(int capacity, java.nio.ByteOrder order) {
		return ByteBuffer.allocate(capacity).order(order);
	}

}
