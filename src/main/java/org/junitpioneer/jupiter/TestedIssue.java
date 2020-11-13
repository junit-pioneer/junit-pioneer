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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the execution result of test method, which is annotated with {@link org.junitpioneer.jupiter.Issue}.
 *
 * In future java this could be a record.
 */
public final class TestedIssue {

	private final String issueId;
	private final List<IssueTestCase> allTests;

	/**
	 * Constructor with all attributes.
	 *
	 * @param issueId Value of the {@link org.junitpioneer.jupiter.Issue} annotation
	 * @param allTests List of all tests, annotated with the issueId
	 */
	public TestedIssue(String issueId, List<IssueTestCase> allTests) {
		this.issueId = issueId;
		this.allTests = allTests;
	}

	/**
	 * Constructor with all attributes.
	 *
	 * @param issueId Value of the {@link org.junitpioneer.jupiter.Issue} annotation
	 */
	public TestedIssue(String issueId) {
		this.issueId = issueId;
		this.allTests = new ArrayList<>();
	}

	/**
	 * Returns the value of the {@link org.junitpioneer.jupiter.Issue} annotation.
	 * @return IssueId the test belongs to
	 */
	public String getIssueId() {
		return issueId;
	}

	/**
	 * Retrieves a list with all test cases related to this issue.
	 *
	 * @return List of all test cases related to this issue
	 */
	public List<IssueTestCase> getAllTests() {
		return allTests;
	}

}
