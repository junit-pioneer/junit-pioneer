/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.cartesian;

import static java.lang.String.format;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.AnnotationConsumerInitializer;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.ReflectionSupport;

final class CartesianParameterArguments {

	private CartesianParameterArguments() {
	}

	static List<?> getSetFromAnnotation(ExtensionContext context, Annotation source, Parameter parameter) {
		try {
			CartesianParameterArgumentsProvider<?> provider = initializeParameterArgumentsProvider(source, parameter);
			return provideArguments(context, parameter, provider);
		}
		catch (Exception ex) {
			throw new ExtensionConfigurationException("Could not provide arguments because of exception.", ex);
		}
	}

	private static CartesianParameterArgumentsProvider<?> initializeParameterArgumentsProvider(Annotation source,
			Parameter parameter) {
		Class<?> providerClass;
		Optional<CartesianArgumentsSource> cartesianProviderAnnotation = AnnotationSupport
				.findAnnotation(parameter, CartesianArgumentsSource.class);
		if (cartesianProviderAnnotation.isPresent()) {
			providerClass = cartesianProviderAnnotation.get().value();
		} else {
			ArgumentsSource providerAnnotation = AnnotationSupport
					.findAnnotation(parameter, ArgumentsSource.class)
					.orElseThrow(() -> new IllegalStateException(
						format("%s was not annotated with %s or %s but should have been.", source.annotationType(),
							CartesianArgumentsSource.class.getName(), ArgumentsSource.class.getName())));
			providerClass = providerAnnotation.value();
		}
		return getAndInitializeCartesianParameterArgumentsProvider(providerClass, parameter);
	}

	private static <T> CartesianParameterArgumentsProvider<?> getAndInitializeCartesianParameterArgumentsProvider(
			Class<T> providerClass, Parameter parameter) {
		T provider = AnnotationConsumerInitializer.initialize(parameter, ReflectionSupport.newInstance(providerClass));
		if (!(provider instanceof CartesianParameterArgumentsProvider)) {
			throw new PreconditionViolationException(format("%s does not implement %s interface.", provider.getClass(),
				CartesianParameterArgumentsProvider.class.getSimpleName()));
		}
		return (CartesianParameterArgumentsProvider<?>) provider;
	}

	private static List<?> provideArguments(ExtensionContext context, Parameter source,
			CartesianParameterArgumentsProvider<?> provider) throws Exception {
		try (Stream<?> arguments = provider.provideArguments(context, source)) {
			return arguments
					.distinct()
					// Keep the original annotation order and remove duplicate values.
					.collect(collectingAndThen(toList(), Collections::unmodifiableList));
		}
	}

}
