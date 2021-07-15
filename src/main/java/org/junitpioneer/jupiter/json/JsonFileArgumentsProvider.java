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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.PreconditionViolationException;
import org.junitpioneer.internal.PioneerPreconditions;
import org.junitpioneer.jupiter.CartesianAnnotationConsumer;

/**
 * The reading of the resources / files is heavily inspired by {@link org.junit.jupiter.params.provider.CsvFileArgumentsProvider}.
 */
class JsonFileArgumentsProvider
		implements ArgumentsProvider, AnnotationConsumer<JsonFileSource>, CartesianAnnotationConsumer<JsonFileSource> {

	private String dataLocation;
	private List<Source> sources;

	@Override
	public void accept(JsonFileSource jsonSource) {
		Stream<Source> resources = Arrays
				.stream(jsonSource.resources())
				.map(JsonFileArgumentsProvider::classpathResource);

		Stream<Source> files = Arrays.stream(jsonSource.files()).map(JsonFileArgumentsProvider::fileResource);

		this.sources = Stream.concat(resources, files).collect(Collectors.toList());
		this.dataLocation = jsonSource.data();
	}

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		JsonConverter jsonConverter = JsonConverterProvider.getJsonConverter(context);

		Method method = context.getRequiredTestMethod();

		return PioneerPreconditions
				.notEmpty(this.sources, "Resources or files must not be empty")
				.stream()
				.map(source -> source.open(context))
				.map(jsonConverter::toNode)
				.flatMap(this::flatMapNode)
				.map(node -> createArguments(method, node));
	}

	private Stream<Node> flatMapNode(Node node) {
		Node nodeForExtraction;
		if (dataLocation == null || dataLocation.isEmpty()) {
			nodeForExtraction = node;
		} else {
			nodeForExtraction = node
					.getNode(dataLocation)
					.orElseThrow(() -> new PreconditionViolationException(
						"Node " + node + " does not have data element at " + dataLocation));
		}
		if (nodeForExtraction.isArray()) {
			return nodeForExtraction.elements();
		}
		return Stream.of(nodeForExtraction);
	}

	private static Arguments createArguments(Method method, Node node) {
		int parameterCount = method.getParameterCount();
		if (parameterCount == 1) {
			// When there is a single parameter the user might want to extract a single value or an entire type.
			// When the parameter has the @Param annotation then a single value needs to be extracted
			Param location = method.getParameters()[0].getAnnotation(Param.class);
			if (location == null) {
				// There is no location -> the node should be converted in the parameter type
				return Arguments.arguments(node.toType(method.getParameters()[0].getType()));
			}

			// Otherwise thread this as method arguments
			return new MethodArguments(method, node);
		}
		return new MethodArguments(method, node);
	}

	private static class MethodArguments implements Arguments {

		protected final Method method;
		protected final Node node;

		public MethodArguments(Method method, Node node) {
			this.method = method;
			this.node = node;
		}

		@Override
		public Object[] get() {
			int parameterCount = method.getParameterCount();
			Object[] arguments = new Object[parameterCount];
			Parameter[] parameters = method.getParameters();
			for (int i = 0; i < parameterCount; i++) {
				Parameter parameter = parameters[i];
				String name;
				Param location = parameter.getAnnotation(Param.class);
				if (location != null) {
					name = location.value();
				} else {
					name = parameter.getName();
				}
				Optional<Node> requestedNode = node.getNode(name);
				if (requestedNode.isPresent()) {
					arguments[i] = requestedNode.get().value(parameter.getType());
				} else {
					arguments[i] = null;
				}
			}
			return arguments;
		}

	}

	private interface Source {

		InputStream open(ExtensionContext context);

	}

	private static Source classpathResource(String resource) {
		return context -> {
			PioneerPreconditions.notBlank(resource, () -> "Classpath resource must not be null or blank");
			InputStream stream = context.getRequiredTestClass().getClassLoader().getResourceAsStream(resource);
			PioneerPreconditions.notNull(stream, () -> "Classpath resource [" + resource + "] does not exist");
			return stream;
		};
	}

	private static Source fileResource(String file) {
		return context -> {
			PioneerPreconditions.notBlank(file, () -> "File must not be null or blank");
			try {
				return Files.newInputStream(Paths.get(file));
			}
			catch (IOException e) {
				throw new UncheckedIOException("Failed to read file " + file, e);
			}
		};
	}

}
