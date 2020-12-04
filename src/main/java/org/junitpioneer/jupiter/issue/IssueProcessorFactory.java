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

import java.util.ServiceLoader;

import org.junitpioneer.jupiter.IssueProcessor;

// Ensures we get the same instance of StoringIssueProcessor in the integration tests
class IssueProcessorFactory {

	private static final ServiceLoader<IssueProcessor> processors = ServiceLoader.load(IssueProcessor.class);

	static boolean hasNext() {
		return processors.iterator().hasNext();
	}

	static ServiceLoader<IssueProcessor> getIssueProcessors() {
		return processors;
	}

}
