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

import java.util.Optional;

import org.junit.platform.engine.TestExecutionResult.Status;
import org.junitpioneer.jupiter.IssueTestCase;

class IssueTestCaseBuilder {

	private final String testId;

	// all of these can be null
	private String issueId;
	private Status result;
	private Long elapsedTime;

	public IssueTestCaseBuilder(String testId) {
		this.testId = testId;
	}

	public IssueTestCaseBuilder setResult(Status result) {
		this.result = result;
		return this;
	}

	public IssueTestCaseBuilder setElapsedTime(long elapsedTime) {
		this.elapsedTime = elapsedTime;
		return this;
	}

	public String getIssueId() {
		return issueId;
	}

	public IssueTestCaseBuilder setIssueId(String issueId) {
		this.issueId = issueId;
		return this;
	}

	public IssueTestCase build() {
		return new IssueTestCase(testId, result, Optional.ofNullable(elapsedTime));
	}

}
