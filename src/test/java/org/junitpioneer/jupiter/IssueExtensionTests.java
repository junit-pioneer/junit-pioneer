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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

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
	void publishAnnotationsValueWithKeyIssueFromMethodAnnotation() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethod(IssueExtensionTests.IssueDummyTestClass.class, "testIsAnnotated");
		assertThat(results).hasNumberOfSucceededTests(1);

		assertThat(results).hasSingleReportEntry().withKeyAndValue("Issue", "Req 11");
	}

	@DisplayName("publishes the class annotation with value 'Req-Class'")
	@Test
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

		@Issue("Req 11")
		@Test
		void testIsAnnotated() {

		}

		@Issue("Req-Class")
		@Nested
		class NestedDummyTestClass {

			@Test
			void shouldRetrieveFromClass() {

			}
		}

	}

}
