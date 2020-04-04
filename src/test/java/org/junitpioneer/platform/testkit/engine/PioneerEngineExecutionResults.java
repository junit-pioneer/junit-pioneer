/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.platform.testkit.engine;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Events;

/**
 * Pioneer own class to handle {@link EngineExecutionResults}.
 */
public class PioneerEngineExecutionResults {

	EngineExecutionResults executionResults;

	public PioneerEngineExecutionResults(Class<?> testClass) {
		executionResults = EngineTestKit
				.engine("junit-jupiter")
				.selectors(DiscoverySelectors.selectClass(testClass))
				.execute();
	}

	public PioneerEngineExecutionResults(Class<?> testClass, String testMethodName) {
		executionResults = EngineTestKit
				.engine("junit-jupiter")
				.selectors(DiscoverySelectors.selectMethod(testClass, testMethodName))
				.execute();
	}

	/**
	 * Get all recorded events.
	 */
	public Events all() {
		return executionResults.all();
	}

	/**
	 * Get recorded events for containers.
	 *
	 * <p>In this context, the word "container" applies to {@link TestDescriptor
	 * TestDescriptors} that return {@code true} from {@link TestDescriptor#isContainer()}.</p>
	 */
	public Events containers() {
		return executionResults.containers();
	}

	/**
	 * Get recorded events for tests.
	 *
	 * <p>In this context, the word "test" applies to {@link TestDescriptor
	 * TestDescriptors} that return {@code true} from {@link TestDescriptor#isTest()}.</p>
	 */
	public Events tests() {
		return executionResults.tests();
	}

}
