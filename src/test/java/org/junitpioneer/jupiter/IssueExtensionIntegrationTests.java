/*
 * Copyright 2016-2022 the original author or authors.
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

@DisplayName("IssueTest extension ")
public class IssueExtensionIntegrationTests {

	/*
	 * It would be nice to run the nested test class with the `PioneerTestKit` (as usual),
	 * then get the created `IssueTestSuite`s and assert the expected results.
	 * Unfortunately, we couldn't make the launched engine use our `TestExecutionListener`
	 * (not even adapted as `EngineExecutionListener`) and so it wouldn't gather the report
	 * entries nor get called when the test kit run finished.
	 *
	 * Hence, we're not able to write a proper integration test for this extension.
	 * Instead, we have to rely on the occasional visual verification that it works - the
	 * `StoringIssueProcessor` prints the gathered information to System.out.
	 *
	 * https://github.com/junit-pioneer/junit-pioneer/issues/375
	 */
	//	@Test
	//	void issueExtensionAndExecutionListenerWorkTogether() {
	//		PioneerTestKit
	//				.executeTestClass(IssueExtensionIntegrationTests.IssueDummyTestClass.class);
	//
	//		StoringIssueProcessor processor = (StoringIssueProcessor) ServiceLoader.load(IssueProcessor.class).iterator().next();
	//		List<IssueTestSuite> testSuites = processor.issueTestSuites();
	//
	//		// assert... but at this point, the list is always null, because
	//		// `testPlanExecutionFinished` wasn't called on the listener
	//	}

	@Nested
	class IssueDummyTestClass {

		@Test
		void testNoAnnotation() {
		}

		@Test
		@Issue("Req 11")
		void testIsAnnotated() {

		}

		@Test
		@Issue("Req 12")
		void failingTestIsAnnotated() {
			// fail();
		}

		@Nested
		@Issue("Req-Class")
		class NestedDummyTestClass {

			@Test
			void shouldRetrieveFromClass() {

			}

			@Test
			void failedShouldRetrieveFromClass() {
				// fail();
			}

		}

	}

}
