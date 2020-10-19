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

@DisplayName("IssueTest extension ")
public class IssueExtensionTests {

	@DisplayName("publishes nothing, if method is not annotated")
	@Test
	void publishNothingIfMethodIsNotAnnotated() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(IssueExtensionTests.IssueDummyTestClass.class, "testNoAnnotation");

		assertThat(results).hasNumberOfSucceededTests(1);
		assertThat(results).hasNumberOfReportEntries(0);
	}

	@DisplayName("publishes the annotations value with key 'Issue'")
	@Test
	void publishAnnotationsValueWithKeyIssue() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(IssueExtensionTests.IssueDummyTestClass.class, "testIsAnnotated");
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
