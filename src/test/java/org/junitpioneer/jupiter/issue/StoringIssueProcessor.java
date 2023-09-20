/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.issue;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;

import org.junitpioneer.jupiter.IssueProcessor;
import org.junitpioneer.jupiter.IssueTestSuite;

/**
 * Simple example service implementation for test usage of {@link IssueProcessor}.
 */
public class StoringIssueProcessor implements IssueProcessor {

	// collected test suites are static to make them accessible for tests
	static final List<IssueTestSuite> ISSUE_TEST_SUITES = new ArrayList<>();

	@Override
	public void processTestResults(List<IssueTestSuite> issueTestSuites) {
		ISSUE_TEST_SUITES.clear();
		ISSUE_TEST_SUITES.addAll(issueTestSuites);

		String suitesString = issueTestSuites
				.stream()
				.map(suite -> suite.issueId() + "\n"
						+ suite.tests().stream().map(Object::toString).collect(joining("\n\t", "\t", "\n")))
				.collect(joining(""));
		System.out.println(suitesString);
	}

}
