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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junitpioneer.internal.PioneerPreconditions;

/**
 * Provides arguments from inline JSON specified with {@link JsonSource}.
 */
class JsonInlineArgumentsProvider extends AbstractJsonArgumentsProvider<JsonSource> {

	private List<String> jsonValues;

	@Override
	public void accept(JsonSource jsonSource) {
		this.jsonValues = Arrays.asList(jsonSource.value());
	}

	@Override
	protected Stream<Node> provideNodes(ExtensionContext context, JsonConverter jsonConverter) {
		return PioneerPreconditions
				.notEmpty(this.jsonValues, "value must not be empty")
				.stream()
				.map(value -> jsonConverter.toNode(value, true))
				.flatMap(node -> node.isArray() ? node.elements() : Stream.of(node));
	}

}
