/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.testkit.assertion;

/**
 * This interface contains methods for asserting tests and containers.
 *
 * There is an inherent problem with the naming of the `thatStarted()` method that you should be aware of:
 * Imagine the following scenario: You have 4 tests, 3 should fail, one should succeed.
 * The logical way to test that would be to write
 * <p>
 *     assertThat(results).hasNumberOfTests(3).thatStarted().thenFailed();
 *     assertThat(results).hasSingleTest().thatStarted().thenSucceeded();
 * </p>
 * Looks good, does not work: it fails on the first line because there were actually 4 tests that started.
 */
public interface TestAssert {

	StartedTestAssert thatStarted();

	FailureAssert thatFailed();

	void thatSucceeded();

	void thatAborted();

	TestAssert dynamicallyRegistered();

}
