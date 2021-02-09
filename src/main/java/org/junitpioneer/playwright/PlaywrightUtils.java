/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.playwright;

import static java.util.stream.Collectors.toList;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;

class PlaywrightUtils {

	static final Namespace PLAYWRIGHT_NAMESPACE = Namespace.create(PlaywrightTests.class);

	static void putIntoStore(ExtensionContext context, CloseableResource resource) {
		context
				.getStore(PLAYWRIGHT_NAMESPACE)
				.getOrComputeIfAbsent("closeableStack", __ -> new CloseableArrayDeque(), CloseableArrayDeque.class)
				.push(resource);
	}

	private static class CloseableArrayDeque extends ArrayDeque<CloseableResource> implements CloseableResource {

		@Override
		public void close() throws Throwable {
			List<Throwable> errors = this.stream().map(resource -> {
				try {
					resource.close();
					return null;
				}
				catch (Throwable ex) {
					return ex;
				}
			}).filter(Objects::nonNull).collect(toList());
			if (!errors.isEmpty()) {
				Exception exception = new Exception("Error while closing resources.");
				errors.forEach(exception::addSuppressed);
				throw exception;
			}
		}

	}

}
