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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.ThrowableAssert;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.Events;

class TestAssertBase extends AbstractPioneerAssert<TestAssertBase, Events> implements TestCaseAssert, FailureAssert {

	TestAssertBase(Events events) {
		super(events, TestAssertBase.class, 1);
	}

	@Override
	public AbstractThrowableAssert<?, ? extends Throwable> withExceptionInstanceOf(
			Class<? extends Throwable> exceptionType) {
		Optional<Throwable> thrown = throwable();
		assertThat(thrown).isPresent();
		assertThat(thrown.get()).isInstanceOf(exceptionType);
		return new ThrowableAssert(thrown.get());
	}

	@Override
	public AbstractThrowableAssert<?, ? extends Throwable> withException() {
		Optional<Throwable> thrown = throwable();
		assertThat(thrown).isPresent();
		return new ThrowableAssert(thrown.get());
	}

	private Optional<Throwable> throwable() {
		return actual
				.failed()
				.stream()
				.findFirst()
				.flatMap(fail -> fail.getPayload(TestExecutionResult.class))
				.flatMap(TestExecutionResult::getThrowable);
	}

	@Override
	public FailureAssert whichFailed() {
		assertThat(actual.failed().count()).isEqualTo(1);
		return this;
	}

	@Override
	public void whichSucceeded() {
		assertThat(actual.succeeded().count()).isEqualTo(1);
	}

	@Override
	public void whichAborted() {
		assertThat(actual.aborted().count()).isEqualTo(1);
	}

}
