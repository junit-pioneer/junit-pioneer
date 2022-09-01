/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.json;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.platform.commons.support.ReflectionSupport;

class ArrayNodeToListConverter implements ArgumentConverter {

	// recreate default constructor to prevent compiler warning
	public ArrayNodeToListConverter() {
	}

	@Override
	public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
		Class<?> parameterType = context.getParameter().getType();
		if (!(List.class.isAssignableFrom(parameterType) && source instanceof ArrayNode)) {
			return source;
		}
		Class<?> actualTypeArgument = (Class<?>) ((ParameterizedType) context.getParameter().getParameterizedType())
				.getActualTypeArguments()[0];
		return createList(parameterType, actualTypeArgument, (ArrayNode) source);
	}

	// We pass the list creation to a generic method, to trick Java into recognizing the element type
	@SuppressWarnings("unchecked")
	private static <T> List<T> createList(Class<?> listType, Class<T> elementType, ArrayNode nodes) {
		List<T> values;
		if (listType.equals(List.class))
			values = (List<T>) ReflectionSupport.newInstance(ArrayList.class);
		else
			values = (List<T>) ReflectionSupport.newInstance(listType);
		ObjectMapper mapper = new ObjectMapper();
		nodes.forEach(node -> {
			try {
				values.add(mapper.treeToValue(node, elementType));
			}
			catch (JsonProcessingException exception) {
				throw new ArgumentConversionException("Could not convert parameter because of a JSON exception.",
					exception);
			}
		});
		return values;
	}

}
