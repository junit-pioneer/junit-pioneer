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
 * This interface contains methods for asserting a single test or a single container.
 */
public interface TestCaseAssert {

	void whichSucceeded();

	void whichAborted();

	FailureAssert whichFailed();

}
