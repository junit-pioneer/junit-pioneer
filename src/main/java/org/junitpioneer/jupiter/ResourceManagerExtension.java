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

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.ReflectionSupport;

public class ResourceManagerExtension implements ParameterResolver {

	private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace
			.create(ResourceManagerExtension.class);

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		// TODO: Only return true if the parameter is annotated with @New or @Shared
		// TODO: Check that we're on a test method, rather than say a constructor or a before-each block?
		// return parameterContext.isAnnotated(New.class);
		return true;
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		New newResourceAnnotation = parameterContext
				.findAnnotation(New.class)
				.orElseThrow(() -> {
					// TODO
					throw new UnsupportedOperationException("TODO");
				});
		ResourceFactory<?> resourceFactory = ReflectionSupport.newInstance(newResourceAnnotation.value());
		// TODO: Put the resourceFactory in the store too?
		Resource<?> resource;
		try {
			resource = resourceFactory.create();
		}
		catch (Exception e) {
			// TODO
			throw new UnsupportedOperationException("TODO");
		}
		// TODO: Check that we're using a custom NAMESPACE
		extensionContext
				.getStore(ExtensionContext.Namespace.GLOBAL)
				.put("theResource", (ExtensionContext.Store.CloseableResource) resource::close);
		try {
			return resource.get();
		}
		catch (Exception e) {
			// TODO
			throw new UnsupportedOperationException("TODO");
		}
	}

}
