/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.issue;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Provider for implementations of the {@link IssueProcessor} interface.
 */
public class IssueProcessorProvider {

	private static IssueProcessorProvider provider;
	private ServiceLoader<IssueProcessor> loader;

	private IssueProcessorProvider() {
		loader = ServiceLoader.load(IssueProcessor.class);
	}

	/**
	 * Retrieve an instance of the IssueProcessorProvider.
	 *
	 * @return Instance of IssueProcessorProvider
	 */
	public static IssueProcessorProvider getInstance() {
		if (null == provider) {
			provider = new IssueProcessorProvider();
		}

		return provider;
	}

	/**
	 * Provides all registered service providers.
	 *
	 * @return All known {@link IssueProcessor}s
	 */
	public Iterator<IssueProcessor> providers() {
		return loader.iterator();
	}

}
