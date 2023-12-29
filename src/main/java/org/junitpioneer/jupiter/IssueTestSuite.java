/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import java.util.List;
import java.util.Objects;

/**
 * Represents the execution result of test method, which is annotated with {@link org.junitpioneer.jupiter.Issue}.
 *
 * <p>Once Pioneer baselines against Java 17, this will be a record.</p>
 *
 * @since 1.1
 * @see Issue
 * @see IssueProcessor
 */
public final class IssueTestSuite {

	private final String issueId;
	private final List<IssueTestCase> tests;

	/**
	 * Constructor with all attributes.
	 *
	 * @param issueId Value of the {@link org.junitpioneer.jupiter.Issue} annotation
	 * @param tests List of all tests, annotated with the issueId
	 */
	public IssueTestSuite(String issueId, List<IssueTestCase> tests) {
		this.issueId = issueId;
		this.tests = List.copyOf(tests);
	}

	/**
	 * Returns the value of the {@link org.junitpioneer.jupiter.Issue} annotation.
	 *
	 * @return IssueId the test belongs to
	 */
	public String issueId() {
		return issueId;
	}

	/**
	 * Retrieves a list with all test cases related to this issue.
	 *
	 * @return List of all test cases related to this issue
	 */
	public List<IssueTestCase> tests() {
		return tests;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof IssueTestSuite))
			return false;
		IssueTestSuite that = (IssueTestSuite) o;
		return issueId.equals(that.issueId) && tests.equals(that.tests);
	}

	@Override
	public int hashCode() {
		return Objects.hash(issueId);
	}

	@Override
	public String toString() {
		return "IssueTestSuite{" + "issueId='" + issueId + '\'' + ", tests=" + tests + '}';
	}

}
