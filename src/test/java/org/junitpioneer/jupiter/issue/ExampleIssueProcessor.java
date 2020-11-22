/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.issue;

import java.util.List;

import org.junitpioneer.jupiter.IssueProcessor;
import org.junitpioneer.jupiter.IssueTestSuite;

/**
 * Simple example service implementation for test usage of {@link IssueProcessor}.
 */
public class ExampleIssueProcessor implements IssueProcessor {

	@Override
	public void processTestResults(List<IssueTestSuite> issueTestSuites) {

		for (IssueTestSuite testSuite : issueTestSuites) {
			System.out.println(testSuite.issueId());
		}

	}

}
