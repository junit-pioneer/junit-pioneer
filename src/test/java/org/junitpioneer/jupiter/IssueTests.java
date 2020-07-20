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

import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

@DisplayName("IssueTest extension")
public class IssueTests {

	@DisplayName("s output should be listed as property in the pioneer report by the PioneerReportListener")
	@Issue("bishue 135")
	@Test
	void writeToReport() {
		// This tests should get covered by the registered PioneerReportListener.
		// In the report should then be a property with key "issue" and value "bishue 135" for this test

		/*
		    <testcase name="writeToReport()" status="SUCCESSFUL">
		        <properties>
		            <property name="Issue" value="bishue 135"/>
		        </properties>
		    </testcase>
		 */

		// The report can't be accessed within this test as the file is written after this (and all other)
		// tests are finished, so we only check the number of started tests in this assertion.
		ExecutionResults results = PioneerTestKit.executeTestClass(IssueTests.IssueDummyTestClass.class);
		assertThat(results).hasNumberOfStartedTests(2);
	}

	@DisplayName(" publishes nothing, if method is not annotated")
	@Test
	void publishNothingIfMethodIsNotAnnotated() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(IssueTests.IssueDummyTestClass.class, "testNoAnnotation");

		assertThat(results).hasNumberOfSucceededTests(1);
		assertThat(results).hasNumberOfReportEntries(0);
	}

	@DisplayName(" publishes the annotations value with key 'Issue'")
	@Test
	void publishAnnotationsValueWithKeyIssue() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(IssueTests.IssueDummyTestClass.class, "testIsAnnotated");
		assertThat(results).hasNumberOfSucceededTests(1);

		assertThat(results).hasSingleReportEntry().withKeyAndValue("Issue", "Req 11");
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
