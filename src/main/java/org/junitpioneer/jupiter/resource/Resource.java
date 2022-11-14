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

import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * {@code Resource} is the common interface for "resources", as managed by {@link ResourceFactory}
 * implementations.
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
 * @param <T> the type of the resource
 * @since 1.9.0
 * @see ResourceFactory
 */
public interface Resource<T> extends ExtensionContext.Store.CloseableResource {

	/**
	 * Returns the contents of the resource.
	 *
	 * @throws Exception if getting the resource failed
	 */
	T get() throws Exception;

	/**
	 * Closes the resource.
	 *
	 * @throws Exception if closing the resource failed
	 */
	@Override
	default void close() throws Exception {
		// no op by default
	}

}
