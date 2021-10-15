/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.internal;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstances;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * Rudimentary implementation of an {@link ExtensionContext} that
 * returns a {@link Class} and {@link Method} that were specified
 * during construction.
 *
 * Best used to text annotation discovery mechanisms that expect an
 * {@code ExtensionContext} as input.
 */
public class TestExtensionContext implements ExtensionContext {

	private static final UnsupportedOperationException NOT_SUPPORTED_IN_TEST_CONTEXT = new UnsupportedOperationException(
		"Not supported in test context");
	private final Class<?> testClass;
	private final Method testMethod;

	public TestExtensionContext(Class<?> testClass, Method testMethod) {
		this.testClass = testClass;
		this.testMethod = testMethod;
	}

	// @Override once we baseline against 5.8
	public ExecutionMode getExecutionMode() {
		throw NOT_SUPPORTED_IN_TEST_CONTEXT;
	}

	@Override
	public Optional<Class<?>> getTestClass() {
		return Optional.of(testClass);
	}

	@Override
	public Optional<Method> getTestMethod() {
		return Optional.of(testMethod);
	}

	@Override
	public Optional<ExtensionContext> getParent() {
		throw NOT_SUPPORTED_IN_TEST_CONTEXT;
	}

	@Override
	public ExtensionContext getRoot() {
		throw NOT_SUPPORTED_IN_TEST_CONTEXT;
	}

	@Override
	public String getUniqueId() {
		throw NOT_SUPPORTED_IN_TEST_CONTEXT;
	}

	@Override
	public String getDisplayName() {
		throw NOT_SUPPORTED_IN_TEST_CONTEXT;
	}

	@Override
	public Set<String> getTags() {
		throw NOT_SUPPORTED_IN_TEST_CONTEXT;
	}

	@Override
	public Optional<AnnotatedElement> getElement() {
		throw NOT_SUPPORTED_IN_TEST_CONTEXT;
	}

	@Override
	public Optional<TestInstance.Lifecycle> getTestInstanceLifecycle() {
		throw NOT_SUPPORTED_IN_TEST_CONTEXT;
	}

	@Override
	public Optional<Object> getTestInstance() {
		throw NOT_SUPPORTED_IN_TEST_CONTEXT;
	}

	@Override
	public Optional<TestInstances> getTestInstances() {
		throw NOT_SUPPORTED_IN_TEST_CONTEXT;
	}

	@Override
	public Optional<Throwable> getExecutionException() {
		throw NOT_SUPPORTED_IN_TEST_CONTEXT;
	}

	@Override
	public Optional<String> getConfigurationParameter(String key) {
		throw NOT_SUPPORTED_IN_TEST_CONTEXT;
	}

	@Override
	public <T> Optional<T> getConfigurationParameter(String key, Function<String, T> transformer) {
		throw NOT_SUPPORTED_IN_TEST_CONTEXT;
	}

	@Override
	public void publishReportEntry(Map<String, String> map) {
		throw NOT_SUPPORTED_IN_TEST_CONTEXT;
	}

	@Override
	public Store getStore(Namespace namespace) {
		throw NOT_SUPPORTED_IN_TEST_CONTEXT;
	}

}
