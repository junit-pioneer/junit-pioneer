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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junitpioneer.AbstractPioneerTestEngineTests;
import org.junitpioneer.jupiter.utils.EventRecorderUtils;

@ExtendWith(IssueExtension.class)
public class IssueTests extends AbstractPioneerTestEngineTests {

	@Test
	void checkMethodNotAnnotated() {
		ExecutionEventRecorder eventRecorder = executeTests(IssueTests.IssueDummyTestClass.class, "testNoAnnotation");

		Map<String, String> reportEntry = EventRecorderUtils.getFirstReportEntry(eventRecorder);
		assertThat(reportEntry).isEmpty();

	}

	@Test
	void checkMethodIstAnnotated() {
		ExecutionEventRecorder eventRecorder = executeTests(IssueTests.IssueDummyTestClass.class, "testIsAnnotated");

		Map<String, String> reportEntry = EventRecorderUtils.getFirstReportEntry(eventRecorder);
		assertThat(reportEntry).hasSize(1);

		String result = reportEntry.get("Issue");
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo("Req 11");
	}

	static class IssueDummyTestClass {

		@Test
		void testNoAnnotation() {

		}

		@Issue("Req 11")
		@Test
		void testIsAnnotated() {

		}

	}

}
