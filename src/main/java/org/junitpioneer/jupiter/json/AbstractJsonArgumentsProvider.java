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

import static java.lang.String.format;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junitpioneer.internal.PioneerPreconditions;
import org.junitpioneer.jupiter.cartesian.CartesianParameterArgumentsProvider;

/**
 * Provides arguments from JSON files specified with {@link JsonFileSource}.
 */
abstract class AbstractJsonArgumentsProvider<A extends Annotation>
		implements ArgumentsProvider, AnnotationConsumer<A>, CartesianParameterArgumentsProvider<Object> {

	public static final String CONFIG_PARAM = "org.junitpioneer.jupiter.json.objectmapper";

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
		String config = context.getConfigurationParameter(CONFIG_PARAM).orElse("default");
		PioneerPreconditions
				.notBlank(config, format("The configuration parameter %s must not have a blank value", CONFIG_PARAM));
		String objectMapperId = AnnotationSupport
				.findAnnotation(context.getRequiredTestMethod(), UseObjectMapper.class)
				.map(UseObjectMapper::value)
				.orElse(config);
		PioneerPreconditions.notBlank(objectMapperId, format("%s must not have a blank value", UseObjectMapper.class));
		return provideNodes(context, JsonConverterProvider.getJsonConverter(objectMapperId));
	}

	protected abstract Stream<Node> provideNodes(ExtensionContext context, JsonConverter jsonConverter);

	private static Object createArgumentForCartesianProvider(Parameter parameter, Node node) {
		Property property = parameter.getAnnotation(Property.class);
		if (property == null) {
			return node.toType(parameter.getType());
		} else {
			return node.getNode(property.value()).map(value -> value.value(parameter.getType())).orElse(null);
		}
	}

	private static Arguments createArguments(Method method, Node node) {
		if (method.getParameterCount() == 1) {
			Parameter onlyParameter = method.getParameters()[0];
			// When there is a single parameter, the user might want to extract a single value or an entire type.
			// When the parameter has the `@Property` annotation, then a single value needs to be extracted.
			Property property = onlyParameter.getAnnotation(Property.class);
			if (property == null) {
				// no property specified -> the node should be converted in the parameter type
				// We must explicitly wrap the return into an Object[] because otherwise the return
				// value is mistakenly interpreted as an Object[] and throws a ClassCastException
				return () -> new Object[] { node.toType(onlyParameter.getParameterizedType()) };
			}

			// otherwise, treat this as method arguments
			return createArgumentsForMethod(method, node);
		}
		return createArgumentsForMethod(method, node);
	}

	private static Arguments createArgumentsForMethod(Method method, Node node) {
		// @formatter:off
		Object[] arguments = Arrays.stream(method.getParameters())
				.map(parameter -> {
					Property property = parameter.getAnnotation(Property.class);
					String name = property == null
							? parameter.getName()
							: property.value();
					return node
							.getNode(name)
							.map(value -> value.value(parameter.getParameterizedType()))
							.orElse(null);
				})
				.toArray();
		// @formatter:on
		return Arguments.of(arguments);
	}

}
