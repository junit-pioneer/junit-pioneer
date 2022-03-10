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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.PreconditionViolationException;
import org.junitpioneer.internal.PioneerPreconditions;
import org.junitpioneer.jupiter.cartesian.CartesianParameterArgumentsProvider;

/**
 * Provides arguments from JSON files specified with {@link JsonFileSource}.
 */
class JsonFileArgumentsProvider
		implements ArgumentsProvider, AnnotationConsumer<JsonFileSource>, CartesianParameterArgumentsProvider<Object> {

	// the reading of the resources / files is heavily inspired by Jupiter's CsvFileArgumentsProvider

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
		Method method = context.getRequiredTestMethod();
		return streamNodes(context).map(node -> createArguments(method, node));
	}

	@Override
	public Stream<Object> provideArguments(ExtensionContext context, Parameter parameter) throws Exception {
		return streamNodes(context).map(node -> createArgumentForCartesianProvider(parameter, node));
	}

	private Stream<Node> streamNodes(ExtensionContext context) {
		JsonConverter jsonConverter = JsonConverterProvider.getJsonConverter(context);
		return PioneerPreconditions
				.notEmpty(this.sources, "Resources or files must not be empty")
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

	static Object createArgumentForCartesianProvider(Parameter parameter, Node node) {
		Property property = parameter.getAnnotation(Property.class);
		if (property == null) {
			return node.toType(parameter.getType());
		} else {
			return node.getNode(property.value()).map(value -> value.value(parameter.getType())).orElse(null);
		}
	}

	static Arguments createArguments(Method method, Node node) {
		boolean singleParameter = method.getParameterCount() == 1;
		if (singleParameter) {
			Parameter onlyParameter = method.getParameters()[0];
			// When there is a single parameter, the user might want to extract a single value or an entire type.
			// When the parameter has the `@Property` annotation, then a single value needs to be extracted.
			Property property = onlyParameter.getAnnotation(Property.class);
			if (property == null) {
				// no property specified -> the node should be converted in the parameter type
				return Arguments.arguments(node.toType(onlyParameter.getType()));
			}

			// otherwise, treat this as method arguments
			return createArgumentsForMethod(method, node);
		}
		return createArgumentsForMethod(method, node);
	}

	static Arguments createArgumentsForMethod(Method method, Node node) {
		// @formatter:off
		Object[] arguments = Arrays.stream(method.getParameters())
				.map(parameter -> {
					Property property = parameter.getAnnotation(Property.class);
					String name = property == null
							? parameter.getName()
							: property.value();
					return node
							.getNode(name)
							.map(value -> value.value(parameter.getType()))
							.orElse(null);
				})
				.toArray();
		// @formatter:on
		return Arguments.of(arguments);
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
