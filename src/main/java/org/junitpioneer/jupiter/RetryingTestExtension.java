/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static java.lang.String.format;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.platform.commons.support.AnnotationSupport;
import org.opentest4j.TestAbortedException;

public class RetryingTestExtension implements TestTemplateInvocationContextProvider, TestExecutionExceptionHandler {

	private static final Namespace NAMESPACE = Namespace.create(RetryingTestExtension.class);

	@Override
	public boolean supportsTestTemplate(ExtensionContext context) {
		// the annotation only applies to methods (see its `@Target`),
		// so it doesn't matter that this method checks meta-annotations
		return PioneerAnnotationUtils.isAnyAnnotationPresent(context, RetryingTest.class);
	}

	@Override
	public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
		FailedTestRetrier retrier = retrierFor(context);
		return stream(spliteratorUnknownSize(retrier, ORDERED), false);
	}

	@Override
	public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
		// this `context` (M) is a child of the context passed to `provideTestTemplateInvocationContexts` (T),
		// which means M's store content is invisible to T's store; this can be fixed by using T's store here
		ExtensionContext templateContext = context
				.getParent()
				.orElseThrow(() -> new IllegalStateException(
					"Extension context \"" + context + "\" should have a parent context."));
		retrierFor(templateContext).failed(throwable);
	}

	private static FailedTestRetrier retrierFor(ExtensionContext context) {
		Method test = context.getRequiredTestMethod();
		return context
				.getStore(NAMESPACE)
				.getOrComputeIfAbsent(test.toString(), __ -> FailedTestRetrier.createFor(test),
					FailedTestRetrier.class);
	}

	private static class FailedTestRetrier implements Iterator<RetryingTestInvocationContext> {

		private final int maxRetries;

		private int retriesSoFar;
		private int exceptionsSoFar;

		private FailedTestRetrier(int maxRetries) {
			this.maxRetries = maxRetries;
			this.retriesSoFar = 0;
			this.exceptionsSoFar = 0;
		}

		static FailedTestRetrier createFor(Method test) {
			RetryingTest retryingTest = AnnotationSupport
					.findAnnotation(test, RetryingTest.class)
					.orElseThrow(() -> new IllegalStateException("@RetryingTest is missing."));
			return new FailedTestRetrier(retryingTest.value());
		}

		void failed(Throwable exception) {
			if (exception instanceof TestAbortedException)
				throw new TestAbortedException("Test execution was skipped, possibly because of a failed assumption.",
					exception);

			exceptionsSoFar++;

			boolean allRetriesFailed = exceptionsSoFar == maxRetries;
			if (allRetriesFailed)
				throw new AssertionError(
					format("Test execution #%d (of up to %d) failed ~> test fails - see cause for details",
						exceptionsSoFar, maxRetries),
					exception);
			else
				throw new TestAbortedException(
					format("Test execution #%d (of up to %d) failed ~> will retry...", exceptionsSoFar, maxRetries),
					exception);
		}

		@Override
		public boolean hasNext() {
			// there's always at least one execution
			if (retriesSoFar == 0)
				return true;

			// if we caught an exception in each execution, each execution failed, including the previous one
			boolean previousFailed = retriesSoFar == exceptionsSoFar;
			boolean maxRetriesReached = retriesSoFar == maxRetries;
			return previousFailed && !maxRetriesReached;
		}

		@Override
		public RetryingTestInvocationContext next() {
			if (!hasNext())
				throw new NoSuchElementException();
			retriesSoFar++;
			return new RetryingTestInvocationContext();
		}

	}

}
