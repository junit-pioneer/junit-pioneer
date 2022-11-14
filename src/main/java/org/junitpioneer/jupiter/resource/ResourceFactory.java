/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.resource;

import java.util.List;

import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * {@code ResourceFactory} is the common interface for "resource factories", which are responsible
 * for creating {@link Resource}s.
 *
 * <p>It is part of the "resources" JUnit Jupiter extension, which pertains to anything that needs
 * to be injected into tests and which may need to be started up or torn down. Temporary
 * directories are a common example.
 *
 * <p>This class is intended for <i>implementors</i> of new kinds of resources.</p>
 *
 * <p>For more details and examples, see
 * <a href="https://junit-pioneer.org/docs/resources/" target="_top">the documentation on resources</a>.</p>
 *
 * @param <T> the type of the resources created by the resource factory
 * @since 1.9.0
 * @see Resource
 */
public interface ResourceFactory<T> extends ExtensionContext.Store.CloseableResource {

	/**
	 * Returns a new resource.
	 *
	 * @param arguments a list of strings to be used to populate or configure the resource
	 * @throws Exception if creating the resource failed
	 */
	Resource<T> create(List<String> arguments) throws Exception;

	/**
	 * Closes the resource factory.
	 *
	 * @throws Exception if closing the resource factory failed
	 */
	@Override
	default void close() throws Exception {
		// no op by default
	}

}
