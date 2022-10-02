/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.LifecycleMethodExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.platform.commons.support.AnnotationSupport;
import org.opentest4j.TestAbortedException;

class NotWorkingExtension implements Extension, TestExecutionExceptionHandler, LifecycleMethodExecutionExceptionHandler,
		BeforeEachCallback, AfterEachCallback {

	private static final Namespace NAMESPACE = Namespace.create(NotWorkingExtension.class);

	/**
	 * {@code Boolean} key indicating whether a 'not working' exception occurred:
	 *
	 * <ul>
	 *   <li>{@code true}: A 'not working' exception occurred; the extension should cause
	 *   the test to be skipped
	 *   <li>{@code false}: No exception occurred; the extension should cause the test to fail
	 *   <li>{@code null}: A regular test abort exception was thrown by user code; the
	 *   extension should do nothing
	 * </ul>
	 */
	private static final String EXCEPTION_OCCURRED_STORE_KEY = "exceptionOccurred";

	/**
	 * No-arg constructor for JUnit to be able to create an instance.
	 */
	public NotWorkingExtension() {
	}

	private Store getStore(ExtensionContext context) {
		return context.getStore(NAMESPACE);
	}

	private static NotWorking getNotWorkingAnnotation(ExtensionContext context) {
		return AnnotationSupport
				.findAnnotation(context.getRequiredTestMethod(), NotWorking.class)
				.orElseThrow(() -> new IllegalStateException("@NotWorking is missing."));

	}

	/**
	 * Returns whether the exception should be preserved and reported as is instead
	 * of considering it an expected 'not working' exception.
	 *
	 * <p>This method is used for exceptions which abort test execution and should
	 * have higher precedence than aborted exceptions thrown by this extension.
	 */
	private static boolean shouldPreserveException(Throwable t) {
		// Note: Ideally would use the same logic JUnit uses to determine if exception is aborting
		// execution, see its class OpenTest4JAndJUnit4AwareThrowableCollector
		return TestAbortedException.class.isInstance(t);
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		getStore(context).put(EXCEPTION_OCCURRED_STORE_KEY, false);
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		NotWorking annotation = getNotWorkingAnnotation(context);

		// null if extension should ignore test result
		Boolean didExceptionOccur = getStore(context).remove(EXCEPTION_OCCURRED_STORE_KEY, Boolean.class);
		if (Boolean.TRUE.equals(didExceptionOccur)) {
			String message = annotation.value();
			if (message.isEmpty()) {
				message = "Test marked as 'not working' failed as expected";
			}

			throw new TestAbortedException(message);
		} else if (Boolean.FALSE.equals(didExceptionOccur)) {
			fail("Test marked as 'not working' succeeded; remove @NotWorking from it");
		}
	}

	@Override
	public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
		Store store = getStore(context);
		if (shouldPreserveException(throwable)) {
			store.remove(EXCEPTION_OCCURRED_STORE_KEY);
			throw throwable;
		} else {
			store.put(EXCEPTION_OCCURRED_STORE_KEY, true);
		}
	}

	@Override
	public void handleAfterEachMethodExecutionException(ExtensionContext context, Throwable throwable)
			throws Throwable {
		Store store = getStore(context);
		if (shouldPreserveException(throwable)) {
			store.remove(EXCEPTION_OCCURRED_STORE_KEY);
			LifecycleMethodExecutionExceptionHandler.super.handleAfterEachMethodExecutionException(context, throwable);
		} else {
			store.put(EXCEPTION_OCCURRED_STORE_KEY, true);
		}
	}

}
