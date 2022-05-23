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

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.PreconditionViolationException;
import org.junitpioneer.internal.PioneerPreconditions;

abstract class AbstractJsonSourceBasedArgumentsProvider<A extends Annotation> extends AbstractJsonArgumentsProvider<A> {

	// the reading of the resources / files is heavily inspired by Jupiter's CsvFileArgumentsProvider

	private String dataLocation;
	private List<Source> sources;

	protected void accept(List<Source> sources, String dataLocation) {
		this.sources = sources;
		this.dataLocation = dataLocation;
	}

	@Override
	protected Stream<Node> provideNodes(ExtensionContext context, JsonConverter jsonConverter) {
		return PioneerPreconditions
				.notEmpty(this.sources, "Value must not be empty")
				.stream()
				.map(source -> source.open(context))
				.map(jsonConverter::toNode)
				.flatMap(this::extractArgumentNodes);
	}

	private Stream<Node> extractArgumentNodes(Node node) {
		// @formatter:off
		Node nodeForExtraction = (dataLocation == null || dataLocation.isEmpty())
				? node
				: node.getNode(dataLocation)
						.orElseThrow(() -> new PreconditionViolationException(
							"Node " + node + " does not have data element at " + dataLocation));
		// @formatter:on
		if (nodeForExtraction.isArray()) {
			return nodeForExtraction.elements();
		}
		return Stream.of(nodeForExtraction);
	}

	interface Source {

		InputStream open(ExtensionContext context);

	}

}
