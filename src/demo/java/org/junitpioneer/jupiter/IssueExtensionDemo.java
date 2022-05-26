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

import java.util.List;

import org.junit.jupiter.api.Test;

public class IssueExtensionDemo {

	// tag::issue_simple[]
	@Test
	@Issue("REQ-123")
	void test() {
		// One of the tests for the issue with the id "REQ-123"
	}
	// end::issue_simple[]

	// tag::issue_processor_sample[]
	public class SimpleProcessor implements IssueProcessor {

		@Override
		public void processTestResults(List<IssueTestSuite> allResults) {
			for (IssueTestSuite testSuite : allResults) {
				System.out.println(testSuite.issueId());
			}
		}

	}

	// end::issue_processor_sample[]
}
