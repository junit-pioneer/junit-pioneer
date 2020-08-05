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

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Publish the specified key-value pair to be consumed by an
 * {@code org.junit.platform.engine.EngineExecutionListener}
 * in order to supply additional information to the reporting
 * infrastructure. This is functionally identical to calling
 * {@link org.junit.jupiter.api.extension.ExtensionContext#publishReportEntry(String, String) ExtensionContext::publishReportEntry}
 * from within the test method.
 *
 * <p>{@code ReportEntry} is repeatable and can be used on methods.
 *
 * <p>This extension does not interact with
 * <a href="https://junit.org/junit5/docs/current/user-guide/#writing-tests-parallel-execution" target="_top">parallel test execution</a>.
 * </p>
 *
 * <p>For more details and examples, see
 * <a href="https://junit-pioneer.org/docs/report-entries/" target="_top">the documentation on <code>Report entries</code></a>.
 * </p>
 *
 * @since 0.5.6
 */
@Repeatable(ReportEntry.ReportEntries.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(ReportEntryExtension.class)
public @interface ReportEntry {

	/**
	 * Specifies the key of the pair that's to be published as a report entry.
	 * Defaults to {@code "value"} and can't be blank.
	 *
	 * @see org.junit.jupiter.api.extension.ExtensionContext#publishReportEntry(String, String) ExtensionContext::publishReportEntry
	 */
	String key() default "value";

	/**
	 * Specifies the value of the pair that's to be published as a report entry.
	 * Can't be blank.
	 *
	 * @see org.junit.jupiter.api.extension.ExtensionContext#publishReportEntry(String, String) ExtensionContext::publishReportEntry
	 */
	String value();

	/**
	 * Specifies when the extension should publish the report entry.
	 * Defaults to {@link org.junitpioneer.jupiter.ReportEntry.PublishCondition#ALWAYS ALWAYS}.
	 * @see PublishCondition
	 */
	PublishCondition when() default PublishCondition.ALWAYS;

	/**
	 * The available values you can choose from to define for which test outcomes
	 * the extension should publish the report entry.
	 *
	 * @since 0.6
	 */
	enum PublishCondition {
		/**
		 * Publish report entry after test run, regardless of its outcome
		 * (this is the default value)
		 */
		ALWAYS,

		/**
		 * Publish report entry after successful test run
		 */
		ON_SUCCESS,

		/**
		 * Publish report entry after failed test run
		 * (i.e. the test must actually run; if it fails during setup, the behavior is undefined)
		 */
		ON_FAILURE,

		/**
		 * Publish report entry after test was aborted
		 */
		ON_ABORTED
	}

	/**
	 * Containing annotation of repeatable {@code ReportEntry}.
	 */
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@ExtendWith(ReportEntryExtension.class)
	@interface ReportEntries {

		ReportEntry[] value();

	}

}
