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

public class TestAssertBase extends AbstractPioneerAssert<TestAssertBase, Events>
		implements TestAssert, StartedTestAssert, FailureAssert {

	TestAssertBase(Events events, int expected) {
		super(events, TestAssertBase.class, expected);
	}

	@Override
	public AbstractThrowableAssert<?, ? extends Throwable> withException(Class<? extends Throwable> exceptionType) {
		Optional<Throwable> thrown = throwable();
		assertThat(thrown).isPresent().containsInstanceOf(exceptionType);
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
	public FailureAssert thenFailed() {
		return thatFailed();
	}

	@Override
	public void thenSucceeded() {
		thatSucceeded();
	}

	@Override
	public void thenAborted() {
		thatAborted();
	}

	@Override
	public StartedTestAssert thatStarted() {
		assertThat(actual.started().count()).isEqualTo(expected);
		return this;
	}

	@Override
	public FailureAssert thatFailed() {
		assertThat(actual.failed().count()).isEqualTo(expected);
		return this;
	}

	@Override
	public void thatSucceeded() {
		assertThat(actual.succeeded().count()).isEqualTo(expected);
	}

	@Override
	public void thatAborted() {
		assertThat(actual.aborted().count()).isEqualTo(expected);
	}

	@Override
	public TestAssert dynamicallyRegistered() {
		assertThat(actual.dynamicallyRegistered().count()).isEqualTo(expected);
		return this;
	}

}
