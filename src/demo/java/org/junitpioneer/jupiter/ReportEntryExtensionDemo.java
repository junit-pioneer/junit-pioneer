/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ReportEntryExtensionDemo {

	// tag::no_report_entry[]
	@Test
	void noReportEntryExtension(TestReporter reporter) {
		// YOUR TEST CODE HERE
		reporter.publishEntry("Hello World!");
	}
	// end::no_report_entry[]

	// tag::report_entry_basic[]
	@Test
	@ReportEntry("Hello World!")
	void simple() {
		// YOUR TEST CODE HERE
	}
	// end::report_entry_basic[]

	// tag::report_entry_multiple[]
	@Test
	@ReportEntry("foo")
	@ReportEntry("bar")
	void multiple() {
		// YOUR TEST CODE HERE
	}
	// end::report_entry_multiple[]

	// tag::report_entry_with_key[]
	@Test
	@ReportEntry(key = "line1", value = "Once upon a midnight dreary")
	@ReportEntry(key = "line2", value = "While I pondered weak and weary")
	void edgarAllanPoe() {
		// YOUR TEST CODE HERE
	}
	// end::report_entry_with_key[]

	// tag::report_entry_publish_condition[]
	@Test
	@ReportEntry(key = "line", value = "success entry", when = ReportEntry.PublishCondition.ON_SUCCESS)
	void sufferingFromSuccess() {
		// YOUR TEST CODE HERE
	}
	// end::report_entry_publish_condition[]

	// tag::report_entry_for_params[]
	@ParameterizedTest
	@CsvSource({ "Hello, 21", "World, 42" })
	@ReportEntry("{1} - {0} - {1}")
	void parameterizedTest(String line, int number) {
		// YOUR TEST CODE HERE
	}
	// end::report_entry_for_params[]

}
