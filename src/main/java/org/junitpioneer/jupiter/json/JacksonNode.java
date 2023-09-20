/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.json;

import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A {@link Node} implementation for Jackson 2.
 */
class JacksonNode implements Node {

	private final ObjectMapper objectMapper;
	private final JsonNode node;

	JacksonNode(ObjectMapper objectMapper, JsonNode node) {
		this.objectMapper = objectMapper;
		this.node = node;
	}

	@Override
	public boolean isArray() {
		return node.isArray();
	}

	@Override
	public Stream<Node> elements() {
		return StreamSupport.stream(node.spliterator(), false).map(element -> new JacksonNode(objectMapper, element));
	}

	@Override
	public <T> T toType(Type type) {
		try {
			return objectMapper.treeToValue(node, objectMapper.constructType(type));
		}
		catch (JsonProcessingException e) {
			throw new UncheckedIOException("Failed to convert to type " + type, e);
		}
	}

	@Override
	public Optional<Node> getNode(String name) {
		JsonNode jsonNode = node.get(name);
		if (jsonNode == null) {
			return Optional.empty();
		}
		return Optional.of(new JacksonNode(objectMapper, jsonNode));
	}

	@Override
	public Object value(Type typeHint) {
		if (node.isTextual()) {
			return node.textValue();
		} else if (node.isInt()) {
			return node.intValue();
		} else if (node.isLong()) {
			return node.longValue();
		} else if (node.isDouble()) {
			return node.doubleValue();
		} else if (node.isBoolean()) {
			return node.booleanValue();
		} else if (node.isShort()) {
			return node.shortValue();
		} else if (node.isFloat()) {
			return node.floatValue();
		} else if (node.isBigDecimal()) {
			return node.decimalValue();
		} else if (node.isBigInteger()) {
			return node.bigIntegerValue();
		} else if (node.isObject() || node.isArray()) {
			return toType(typeHint);
		}
		return node;
	}

	@Override
	public String toString() {
		return "JacksonNode{" + "node=" + node + '}';
	}

}
