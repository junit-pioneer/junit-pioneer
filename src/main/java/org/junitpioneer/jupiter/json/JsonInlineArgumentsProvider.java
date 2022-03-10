/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.json;

import static org.junitpioneer.jupiter.json.JsonFileArgumentsProvider.createArgumentForCartesianProvider;
import static org.junitpioneer.jupiter.json.JsonFileArgumentsProvider.createArguments;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junitpioneer.internal.PioneerPreconditions;
import org.junitpioneer.jupiter.cartesian.CartesianParameterArgumentsProvider;

/**
 * Provides arguments from inline JSON specified with {@link JsonSource}.
 */
class JsonInlineArgumentsProvider
		implements ArgumentsProvider, AnnotationConsumer<JsonSource>, CartesianParameterArgumentsProvider<Object> {

	private List<String> jsonValues;

	@Override
	public void accept(JsonSource jsonSource) {
		this.jsonValues = Arrays.asList(jsonSource.value());
	}

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		Method method = context.getRequiredTestMethod();
		return provideNodes(context).map(node -> createArguments(method, node));
	}

	@Override
	public Stream<Object> provideArguments(ExtensionContext context, Parameter parameter) throws Exception {
		return provideNodes(context).map(node -> createArgumentForCartesianProvider(parameter, node));
	}

	private Stream<Node> provideNodes(ExtensionContext context) {
		JsonConverter jsonConverter = JsonConverterProvider.getJsonConverter(context);
		return PioneerPreconditions
				.notEmpty(this.jsonValues, "value must not be empty")
				.stream()
				.map(value -> jsonConverter.toNode(value, true))
				.flatMap(node -> node.isArray() ? node.elements() : Stream.of(node));
	}

}
