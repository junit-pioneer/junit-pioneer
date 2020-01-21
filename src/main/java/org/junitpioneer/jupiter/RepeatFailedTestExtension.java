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
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.platform.commons.support.AnnotationSupport;
import org.opentest4j.TestAbortedException;

public class RepeatFailedTestExtension implements TestTemplateInvocationContextProvider, TestExecutionExceptionHandler {

	private static final Namespace NAMESPACE = Namespace.create("org", "codefx", "RepeatFailedTestExtension");

	@Override
	public boolean supportsTestTemplate(ExtensionContext context) {
		// the annotation only applies to methods (see its `@Target`),
		// so it doesn't matter that this method checks meta-annotations
		return Utils.annotationPresentOnTestMethod(context, RepeatFailedTest.class);
	}

	@Override
	public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
		FailedTestRepeater repeater = repeaterFor(context);
		return stream(spliteratorUnknownSize(repeater, ORDERED), false);
	}

	@Override
	public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
		// this `context` (M) is a child of the context passed to `provideTestTemplateInvocationContexts` (T),
		// which means M's store content is invisible to T's store; this can be fixed by using T's store here
		//@formatter:off
		ExtensionContext templateContext = context
				.getParent()
				.orElseThrow(
						() -> new IllegalStateException(
								"Extension context \"" + context + "\" should have a parent context."));
		//@formatter:on
		repeaterFor(templateContext).failed(throwable);
	}

	private static FailedTestRepeater repeaterFor(ExtensionContext context) {
		//@formatter:off
		Method repeatedTest = context.getRequiredTestMethod();
		return context
				.getStore(NAMESPACE)
				.getOrComputeIfAbsent(
						repeatedTest.toString(),
						__ -> FailedTestRepeater.createFor(repeatedTest),
						FailedTestRepeater.class);
		//@formatter:on
	}

	private static class FailedTestRepeater implements Iterator<RepeatFailedTestInvocationContext> {

		private final int maxRepetitions;

		private int repetitionsSoFar;
		private int exceptionsSoFar;

		private FailedTestRepeater(int maxRepetitions) {
			this.maxRepetitions = maxRepetitions;
			this.repetitionsSoFar = 0;
			this.exceptionsSoFar = 0;
		}

		static FailedTestRepeater createFor(Method repeatedTest) {
			//@formatter:off
			RepeatFailedTest repeatFailedTest = AnnotationSupport.findAnnotation(repeatedTest, RepeatFailedTest.class)
					.orElseThrow(() -> new IllegalStateException("@RepeatFailedTest is missing."));
			return new FailedTestRepeater(repeatFailedTest.value());
			//@formatter:on
		}

		void failed(Throwable exception) throws Throwable {
			exceptionsSoFar++;

			boolean allRepetitionsFailed = exceptionsSoFar == maxRepetitions;
			if (allRepetitionsFailed)
				throw new AssertionError(
					format("Test execution #%d (of up to %d) failed ~> test fails - see cause for details",
						exceptionsSoFar, maxRepetitions),
					exception);
			else
				throw new TestAbortedException(
					format("Test execution #%d (of up to %d) failed ~> will retry...", exceptionsSoFar, maxRepetitions),
					exception);
		}

		@Override
		public boolean hasNext() {
			// there's always at least one execution
			if (repetitionsSoFar == 0)
				return true;

			// if we caught an exception in each repetition, each repetition failed, including the previous one
			boolean previousFailed = repetitionsSoFar == exceptionsSoFar;
			boolean maxRepetitionsReached = repetitionsSoFar == maxRepetitions;
			return previousFailed && !maxRepetitionsReached;
		}

		@Override
		public RepeatFailedTestInvocationContext next() {
			repetitionsSoFar++;
			return new RepeatFailedTestInvocationContext();
		}

	}

}
