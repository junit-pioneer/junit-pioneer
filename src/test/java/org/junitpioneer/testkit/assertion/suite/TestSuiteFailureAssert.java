/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.testkit.assertion.suite;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.assertj.core.api.ListAssert;

interface TestSuiteFailureAssert {

	/**
	 * Asserts that all thrown exceptions are of certain types.
	 *
	 * @param exceptionTypes the exception types you want to check
	 * @return a {@link ListAssert} for asserting exception messages
	 */
	ListAssert<String> withExceptionInstancesOf(Class<? extends Throwable>... exceptionTypes);

	/**
	 * Asserts that all failed tests failed because of a Throwable.
	 *
	 * @return a {@link ListAssert} for asserting exception messages
	 */
	ListAssert<String> withExceptions();

	/**
	 * Asserts that the thrown exceptions fulfill the condition of the given predicate.
	 *
	 * @param predicate the condition the exceptions must fulfill
	 */
	void assertingExceptions(Predicate<List<Throwable>> predicate);

	/**
	 * Applies the supplied consumer to the exceptions thrown by the tests/containers.
	 *
	 * @param testFunction a consumer, for writing more flexible tests
	 */
	void andThenCheckExceptions(Consumer<List<Throwable>> testFunction);

}
