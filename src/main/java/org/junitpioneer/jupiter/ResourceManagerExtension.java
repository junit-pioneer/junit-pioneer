/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.ReflectionSupport;

final class ResourceManagerExtension implements ParameterResolver {

	private static final ExtensionContext.Namespace NAMESPACE = //
		ExtensionContext.Namespace.create(ResourceManagerExtension.class);

	private final AtomicLong resourceIdGenerator = new AtomicLong(0);

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		// TODO: Also return true if the parameter is annotated with @Shared.
		// TODO: Check that the parameter is not annotated with both @New and @Shared.
		return parameterContext.isAnnotated(New.class) || parameterContext.isAnnotated(Shared.class);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		Optional<New> newAnnotation = parameterContext.findAnnotation(New.class);
		Optional<Shared> sharedAnnotation = parameterContext.findAnnotation(Shared.class);
		if (newAnnotation.isPresent()) {
			return resolve(newAnnotation.get(), extensionContext.getStore(NAMESPACE));
		} else if (sharedAnnotation.isPresent()) {
			return resolve(sharedAnnotation.get(), extensionContext.getStore(NAMESPACE));
		} else {
			throw new ParameterResolutionException(String
					.format( //
						"Parameter `%s` on %s is not annotated with @New", //
						parameterContext.getParameter(), //
						extensionContext
								.getTestMethod()
								.map(method -> "method `" + method + '`')
								.orElse("unknown method")));
		}
	}

	private Object resolve(New newAnnotation, ExtensionContext.Store store) {
		ResourceFactory<?> resourceFactory = ReflectionSupport.newInstance(newAnnotation.value());
		// @formatter:off
		store.put(
				resourceIdGenerator.getAndIncrement(),
				(ExtensionContext.Store.CloseableResource) resourceFactory::close);
		// @formatter:on
		Resource<?> resource;
		try {
			resource = resourceFactory.create(unmodifiableList(asList(newAnnotation.arguments())));
		}
		catch (Exception e) {
			throw new ParameterResolutionException(
				"Unable to create an instance of `" + resourceFactory.getClass() + '`', e);
		}
		store.put(resourceIdGenerator.getAndIncrement(), (ExtensionContext.Store.CloseableResource) resource::close);
		try {
			return resource.get();
		}
		catch (Exception e) {
			throw new ParameterResolutionException("Unable to create an instance of `" + resource.getClass() + '`', e);
		}
	}

	private Object resolve(Shared sharedAnnotation, ExtensionContext.Store store) {
		ResourceFactory<?> resourceFactory = ReflectionSupport.newInstance(sharedAnnotation.factory());
		// TODO: Put resourceFactory in store.
		Resource<?> resource;
		try {
			resource = resourceFactory.create(unmodifiableList(asList(sharedAnnotation.arguments())));
		}
		catch (Exception e) {
			// TODO
			throw new UnsupportedOperationException("TODO");
		}
		store.put(resourceIdGenerator.getAndIncrement(), (ExtensionContext.Store.CloseableResource) resource::close);
		try {
			return resource.get();
		}
		catch (Exception e) {
			// TODO
			throw new UnsupportedOperationException("TODO");
		}
	}

}
