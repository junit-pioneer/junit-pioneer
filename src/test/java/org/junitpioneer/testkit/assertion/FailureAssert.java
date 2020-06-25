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

import org.assertj.core.api.AbstractThrowableAssert;

/**
 * Used to assert a single failed container or test.
 */
public interface FailureAssert {

	/**
	 * Asserts that the test/container failed because of a specific type of exception.
	 * @param exceptionType the expected type of the thrown exception
	 * @return an {@link AbstractThrowableAssert} for further assertions
	 */
	AbstractThrowableAssert<?, ? extends Throwable> withExceptionInstanceOf(Class<? extends Throwable> exceptionType);

	/**
	 * Asserts that the test/container failed because an exception was thrown.
	 * @return an {@link AbstractThrowableAssert} for further assertions
	 */
	AbstractThrowableAssert<?, ? extends Throwable> withException();

}
