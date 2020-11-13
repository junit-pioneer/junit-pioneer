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

/**
 * Represents the execution result of test method, which is annotated with {@link org.junitpioneer.jupiter.Issue}.
 *
 * In future java this could be a record.
 */
public final class IssuedTestCase {

	private final String uniqueName;
	private final String issueId;
	private final String result;

	/**
	 * Constructor with all attributes.
	 *
	 * @param uniqueName Unique name of the test method
	 * @param issueId Value of the {@link org.junitpioneer.jupiter.Issue} annotation
	 * @param result Result of the execution
	 */
	public IssuedTestCase(String uniqueName, String issueId, String result) {
		this.uniqueName = uniqueName;
		this.issueId = issueId;
		this.result = result;
	}

	/**
	 * Returns the unique name of the test method.
	 * @return Unique name of the test method
	 */
	public String getUniqueName() {
		return uniqueName;
	}

	/**
	 * Returns the value of the {@link org.junitpioneer.jupiter.Issue} annotation.
	 * @return IssueId the test belongs to
	 */
	public String getIssueId() {
		return issueId;
	}

	/**
	 * Returns the result of the test methods execution.
	 *
	 * @return Result of the test methods execution.
	 */
	public String getResult() {
		return result;
	}

	@Override
	public String toString() {
		return "IssuedTestCase{" + "uniqueName='" + uniqueName + '\'' + ", issueId='" + issueId + '\'' + ", result='"
				+ result + '\'' + '}';
	}

}
