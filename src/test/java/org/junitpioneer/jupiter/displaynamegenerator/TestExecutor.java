/*
 * Copyright 2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.displaynamegenerator;

import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;

public class TestExecutor {

	private final EngineExecutionListener listener;

	public TestExecutor(EngineExecutionListener listener) {
		this.listener = listener;
	}

	public void execute(TestDescriptor descriptor) {
		listener.executionStarted(descriptor);
		try {
			// Execute the test method here
			if (descriptor.isTest()) {
				// Simulate test execution
				listener.executionFinished(descriptor, TestExecutionResult.successful());
			} else {
				for (TestDescriptor child : descriptor.getChildren()) {
					execute(child);
				}
				listener.executionFinished(descriptor, TestExecutionResult.successful());
			}
		}
		catch (Throwable t) {
			listener.executionFinished(descriptor, TestExecutionResult.failed(t));
		}
	}

}
