/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.testkit;

import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Events;

/**
 * Pioneers' class to handle JUnit Jupiter's {@link org.junit.platform.testkit.engine.EngineExecutionResults}.
 * <p>
 * Instantiate with the static factory methods in {@link PioneerTestKit}.
 */
public class ExecutionResults {

	EngineExecutionResults executionResults;

	ExecutionResults(Class<?> testClass) {
		executionResults = EngineTestKit
				.engine("junit-jupiter")
				.selectors(DiscoverySelectors.selectClass(testClass))
				.execute();
	}

	ExecutionResults(Class<?> testClass, String testMethodName) {
		executionResults = EngineTestKit
				.engine("junit-jupiter")
				.selectors(DiscoverySelectors.selectMethod(testClass, testMethodName))
				.execute();
	}

	ExecutionResults(Class<?> testClass, String testMethodName, String methodParameterTypes) {
		executionResults = EngineTestKit
				.engine("junit-jupiter")
				.selectors(DiscoverySelectors.selectMethod(testClass, testMethodName, methodParameterTypes))
				.execute();
	}

	/**
	 * Get all recorded events.
	 */
	public Events allEvents() {
		return executionResults.all();
	}

	/**
	 * Get recorded dynamically registered events.
	 */
	public Events dynamicallyRegisteredEvents() {
		return executionResults.all().dynamicallyRegistered();
	}

	/**
	 * Get recorded events for containers.
	 *
	 * <p>In this context, the word "container" applies to {@link org.junit.platform.engine.TestDescriptor
	 * TestDescriptors} that return {@code true} from {@link org.junit.platform.engine.TestDescriptor#isContainer()}.</p>
	 */
	public Events containerEvents() {
		return executionResults.containers();
	}

	/**
	 * Get recorded events for tests.
	 *
	 * <p>In this context, the word "test" applies to {@link org.junit.platform.engine.TestDescriptor
	 * TestDescriptors} that return {@code true} from {@link org.junit.platform.engine.TestDescriptor#isTest()}.</p>
	 */
	public Events testEvents() {
		return executionResults.tests();
	}

}
