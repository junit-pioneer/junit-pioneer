/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.testkit;

import java.util.List;

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

	private static final String JUPITER_ENGINE_NAME = "junit-jupiter";

	ExecutionResults(Class<?> testClass) {
		executionResults = EngineTestKit
				.engine(JUPITER_ENGINE_NAME)
				.selectors(DiscoverySelectors.selectClass(testClass))
				.execute();
	}

	ExecutionResults(Class<?> testClass, String testMethodName) {
		executionResults = getConfiguredJupiterEngine()
				.selectors(DiscoverySelectors.selectMethod(testClass, testMethodName))
				.execute();
	}

	ExecutionResults(Class<?> testClass, String testMethodName, String methodParameterTypes) {
		executionResults = getConfiguredJupiterEngine()
				.selectors(DiscoverySelectors.selectMethod(testClass, testMethodName, methodParameterTypes))
				.execute();
	}

	ExecutionResults(List<Class<?>> enclosingClasses, Class<?> testClass) {
		executionResults = EngineTestKit
				.engine(JUPITER_ENGINE_NAME)
				.selectors(DiscoverySelectors.selectNestedClass(enclosingClasses, testClass))
				.execute();
	}

	ExecutionResults(List<Class<?>> enclosingClasses, Class<?> testClass, String testMethodName) {
		executionResults = EngineTestKit
				.engine(JUPITER_ENGINE_NAME)
				.selectors(DiscoverySelectors.selectNestedMethod(enclosingClasses, testClass, testMethodName))
				.execute();
	}

	ExecutionResults(List<Class<?>> enclosingClasses, Class<?> testClass, String testMethodName,
			String methodParameterTypes) {
		executionResults = EngineTestKit
				.engine(JUPITER_ENGINE_NAME)
				.selectors(DiscoverySelectors
						.selectNestedMethod(enclosingClasses, testClass, testMethodName, methodParameterTypes))
				.execute();
	}

	private EngineTestKit.Builder getConfiguredJupiterEngine() {
		return EngineTestKit
				.engine(JUPITER_ENGINE_NAME)
				// to tease out concurrency-related bugs, we want parallel execution of our tests
				// (for details, see section "Thread-safety" in CONTRIBUTING.md)
				.configurationParameter("junit.jupiter.execution.parallel.enabled", "true")
				.configurationParameter("junit.jupiter.execution.parallel.mode.default", "concurrent")
				// since we have full control over which tests we execute with this engine,
				// we can parallelize more aggressively than in the general settings in `junit-platform.properties`
				.configurationParameter("junit.jupiter.execution.parallel.mode.classes.default", "concurrent")
				.configurationParameter("junit.jupiter.execution.parallel.config.strategy", "dynamic")
				.configurationParameter("junit.jupiter.execution.parallel.config.dynamic.factor", "1");
	}

	/**
	 * Get all recorded events.
	 */
	public Events allEvents() {
		return executionResults.allEvents();
	}

	/**
	 * Get recorded dynamically registered events.
	 */
	public Events dynamicallyRegisteredEvents() {
		return executionResults.allEvents().dynamicallyRegistered();
	}

	/**
	 * Get recorded events for containers.
	 *
	 * <p>In this context, the word "container" applies to {@link org.junit.platform.engine.TestDescriptor
	 * TestDescriptors} that return {@code true} from {@link org.junit.platform.engine.TestDescriptor#isContainer()}.</p>
	 */
	public Events containerEvents() {
		return executionResults.containerEvents();
	}

	/**
	 * Get recorded events for tests.
	 *
	 * <p>In this context, the word "test" applies to {@link org.junit.platform.engine.TestDescriptor
	 * TestDescriptors} that return {@code true} from {@link org.junit.platform.engine.TestDescriptor#isTest()}.</p>
	 */
	public Events testEvents() {
		return executionResults.testEvents();
	}

}
