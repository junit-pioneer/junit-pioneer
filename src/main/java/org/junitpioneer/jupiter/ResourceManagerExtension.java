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

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return parameterContext.isAnnotated(New.class) ^ parameterContext.isAnnotated(Shared.class);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		Optional<New> newAnnotation = parameterContext.findAnnotation(New.class);
		if (newAnnotation.isPresent()) {
			return resolve(newAnnotation.get(), extensionContext.getStore(NAMESPACE));
		}
		Optional<Shared> sharedAnnotation = parameterContext.findAnnotation(Shared.class);
		if (sharedAnnotation.isPresent()) {
			return resolve(sharedAnnotation.get(), extensionContext.getRoot().getStore(NAMESPACE));
		}
		throw new ParameterResolutionException(String
				.format( //
					"Parameter `%s` on %s is not annotated with @New or @Shared", //
					parameterContext.getParameter(), //
					extensionContext
							.getTestMethod()
							.map(method -> "method `" + method + '`')
							.orElse("an unknown method")));
	}

	private Object resolve(New newAnnotation, ExtensionContext.Store store) {
		ResourceFactory<?> resourceFactory = ReflectionSupport.newInstance(newAnnotation.value());
		store.put(newKey(), resourceFactory);
		Resource<?> resource;
		try {
			resource = resourceFactory.create(unmodifiableList(asList(newAnnotation.arguments())));
		}
		catch (Exception e) {
			throw new ParameterResolutionException(
				"Unable to create a resource from `" + resourceFactory.getClass() + '`', e);
		}
		store.put(newKey(), resource);
		try {
			return resource.get();
		}
		catch (Exception e) {
			throw new ParameterResolutionException(
				"Unable to get the contents of the resource created by `" + resourceFactory.getClass() + '`', e);
		}
	}

	private long newKey() {
		return keyGenerator.getAndIncrement();
	}

	private final AtomicLong keyGenerator = new AtomicLong(0);

	private Object resolve(Shared sharedAnnotation, ExtensionContext.Store store) {
		throwIfConflicting(sharedAnnotation, store);

		ResourceFactory<?> resourceFactory = store
				.getOrComputeIfAbsent(//
					factoryKey(sharedAnnotation), //
					__ -> ReflectionSupport.newInstance(sharedAnnotation.factory()), //
					ResourceFactory.class);
		Resource<?> resource;
		try {
			resource = store.getOrComputeIfAbsent(key(sharedAnnotation), __ -> {
				try {
					return resourceFactory.create(unmodifiableList(asList(sharedAnnotation.arguments())));
				}
				catch (Exception e) {
					throw new UncheckedParameterResolutionException(new ParameterResolutionException(
						"Unable to create a resource from `" + sharedAnnotation.factory() + "`", e));
				}
			}, Resource.class);
		}
		catch (UncheckedParameterResolutionException e) {
			throw e.getCause();
		}
		try {
			return resource.get();
		}
		catch (Exception e) {
			throw new ParameterResolutionException(
				"Unable to get the contents of the resource created by `" + sharedAnnotation.factory() + '`', e);
		}

	}

	private void throwIfConflicting(Shared sharedAnnotation, ExtensionContext.Store store) {
		ResourceFactory<?> presentResourceFactory = //
			store.getOrDefault(factoryKey(sharedAnnotation), ResourceFactory.class, null);

		if (presentResourceFactory == null) {
			store.put(keyOfFactoryKey(sharedAnnotation), factoryKey(sharedAnnotation));
		} else {
			String presentResourceFactoryName = //
				store.getOrDefault(keyOfFactoryKey(sharedAnnotation), String.class, null);

			if (factoryKey(sharedAnnotation).equals(presentResourceFactoryName)
					&& !sharedAnnotation.factory().equals(presentResourceFactory.getClass())) {
				throw new ParameterResolutionException(String
						.format("Two or more parameters are annotated with @Shared annotations with the name \"%s\" "
								+ "but with different factory classes",
							sharedAnnotation.name()));
			}
		}
	}

	private String key(Shared sharedAnnotation) {
		return sharedAnnotation.name() + " resource";
	}

	private String factoryKey(Shared sharedAnnotation) {
		return sharedAnnotation.name() + " resource factory";
	}

	private String keyOfFactoryKey(Shared sharedAnnotation) {
		return sharedAnnotation.name() + " resource factory key";
	}

	private static final class UncheckedParameterResolutionException extends RuntimeException {

		public UncheckedParameterResolutionException(ParameterResolutionException cause) {
			super(cause);
		}

		@Override
		public synchronized ParameterResolutionException getCause() {
			return (ParameterResolutionException) super.getCause();
		}

	}

}
