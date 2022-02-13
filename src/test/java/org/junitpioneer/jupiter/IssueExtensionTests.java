/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static org.junitpioneer.jupiter.issue.IssueExtensionExecutionListener.REPORT_ENTRY_KEY;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

@DisplayName("IssueTest extension ")
public class IssueExtensionTests {

	@Test
	@DisplayName("publishes nothing, if method is not annotated")
	void publishNothingIfMethodIsNotAnnotated() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(IssueExtensionTestCases.class, "testNoAnnotation");

		assertThat(results).hasNumberOfSucceededTests(1);
		assertThat(results).hasNumberOfReportEntries(0);
	}

	@Test
	@DisplayName("publishes the annotations value with key 'Issue'")
	void publishAnnotationsValueWithKeyIssueFromMethodAnnotation() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(IssueExtensionTestCases.class, "testIsAnnotated");
		assertThat(results).hasNumberOfSucceededTests(1);

		assertThat(results).hasSingleReportEntry().withKeyAndValue(REPORT_ENTRY_KEY, "Req 11");
	}

	@Test
	@DisplayName("publishes the class annotation with value 'Req-Class'")
	void publishAnnotationsFromClass() {
		ExecutionResults results = PioneerTestKit
				.executeTestClass(IssueExtensionTestCases.NestedIssueExtensionTestCases.class);
		assertThat(results).hasNumberOfSucceededTests(1);

		assertThat(results).hasSingleReportEntry().withKeyAndValue(REPORT_ENTRY_KEY, "Req-Class");
	}

	static class IssueExtensionTestCases {

		@Test
		void testNoAnnotation() {

		}

		@Test
		@Issue("Req 11")
		void testIsAnnotated() {

		}

		@Nested
		@Issue("Req-Class")
		class NestedIssueExtensionTestCases {

			@Test
			void shouldRetrieveFromClass() {

			}

		}

	}

}
