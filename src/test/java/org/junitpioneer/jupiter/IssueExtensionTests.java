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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

@DisplayName("IssueTest extension ")
public class IssueExtensionTests {

	@Test
	@DisplayName("publishes nothing, if method is not annotated")
	void publishNothingIfMethodIsNotAnnotated() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(IssueExtensionTests.IssueDummyTestClass.class, "testNoAnnotation");

		assertThat(results).hasNumberOfSucceededTests(1);
		assertThat(results).hasNumberOfReportEntries(0);
	}

	@Test
	@DisplayName("publishes the annotations value with key 'Issue'")
	void publishAnnotationsValueWithKeyIssueFromMethodAnnotation() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(IssueExtensionTests.IssueDummyTestClass.class, "testIsAnnotated");
		assertThat(results).hasNumberOfSucceededTests(1);

		assertThat(results).hasSingleReportEntry().withKeyAndValue("Issue", "Req 11");
	}

	@Test
	@DisplayName("publishes the class annotation with value 'Req-Class'")
	void publishAnnotationsFromClass() {
		ExecutionResults results = PioneerTestKit
				.executeTestClass(IssueExtensionTests.IssueDummyTestClass.NestedDummyTestClass.class);
		assertThat(results).hasNumberOfSucceededTests(1);

		assertThat(results).hasSingleReportEntry().withKeyAndValue("Issue", "Req-Class");
	}

	static class IssueDummyTestClass {

		@Test
		void testNoAnnotation() {

		}

		@Test
		@Issue("Req 11")
		void testIsAnnotated() {

		}

		@Nested
		@Issue("Req-Class")
		class NestedDummyTestClass {

			@Test
			void shouldRetrieveFromClass() {

			}

		}

	}

}
