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

import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * A single issue.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "testCases" })
@XmlRootElement(name = "issue")
public class Issue {

	@XmlAttribute(name = "issueId", required = true)
	protected String issueId;
	@XmlAttribute(name = "description", required = false)
	protected String description;
	protected TestCases testCases;

	public Issue() {
		this.testCases = new TestCases();
	}

	public Issue(String issueId) {
		this.issueId = issueId;
		this.testCases = new TestCases();
	}

	public Issue(String issueId, String description) {
		this.issueId = issueId;
		this.description = description;
		this.testCases = new TestCases();
	}

	public Optional<String> getDescription() {
		return Optional.ofNullable(description);
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIssueId() {
		return issueId;
	}

	public void setIssueId(String issueId) {
		this.issueId = issueId;
	}

	public TestCases getTestCases() {
		return testCases;
	}

	public void setTestCases(TestCases testCases) {
		this.testCases = testCases;
	}

}
