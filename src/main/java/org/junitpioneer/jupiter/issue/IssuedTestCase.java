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

/**
 * Represents an @Issue-annotated test method and its execution result.
 *
 * In future java this could be a record.
 */
public final class IssuedTestCase {

	protected String uniqueName;
	protected String issueId;
	protected String status;

	public IssuedTestCase(String uniqueName, String issueId, String status) {
		this.uniqueName = uniqueName;
		this.issueId = issueId;
		this.status = status;
	}

	public String getUniqueName() {
		return uniqueName;
	}

	public String getIssueId() {
		return issueId;
	}

	public String getStatus() {
		return status;
	}
}
