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

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Publish the specified key-value pair to be consumed by an
 * {@code org.junit.platform.engine.EngineExecutionListener}
 * in order to supply additional information to the reporting
 * infrastructure. This is funtionally identical to calling
 * {@link org.junit.jupiter.api.extension.ExtensionContext#publishReportEntry(String, String) ExtensionContext::publishReportEntry}
 * from within the test method.
 */
@Repeatable(ReportEntries.class)
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

}
