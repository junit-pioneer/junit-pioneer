/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.testkit.assertion.single;

import org.assertj.core.api.AbstractThrowableAssert;

/**
 * Used to assert a single aborted container or test.
 */
public interface TestCaseAbortedAssert {

	/**
	 * Asserts that the test/container was aborted because of a specific type of exception.
	 *
	 * @param exceptionType the expected type of the thrown exception
	 * @return an {@link AbstractThrowableAssert} for further assertions
	 */
	<T extends Throwable> AbstractThrowableAssert<?, T> withExceptionInstanceOf(Class<T> exceptionType);

}
