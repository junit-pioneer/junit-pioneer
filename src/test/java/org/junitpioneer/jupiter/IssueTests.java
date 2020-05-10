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
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

public class IssueTests {

	@Issue("bishue 135")
	@Test
	void writeToReport() {
		// This tests should get covered by the registered PioneerReportListener.
		// In the report should then be an property with key "issue" and value "bishue 135" for this test

		/*
		    <testcase name="writeToReport()" status="SUCCESSFUL">
		        <properties>
		            <property name="Issue" value="bishue 135"/>
		        </properties>
		    </testcase>
		 */

		// The report can't be accessed within this test as the file is written after this (and all others)
		// tests are finished
		assertThat(true).isTrue();
	}

	@Test
	void checkMethodNotAnnotated() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(IssueTests.IssueDummyTestClass.class, "testNoAnnotation");

		assertThat(results.numberOfSucceededTests()).isEqualTo(1);
		assertThat(results.reportEntries()).isEmpty();
	}

	@Test
	void checkMethodIsAnnotated() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(IssueTests.IssueDummyTestClass.class, "testIsAnnotated");
		assertThat(results.numberOfSucceededTests()).isEqualTo(1);

		Map<String, String> reportEntry = results.reportEntries().get(0);
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
