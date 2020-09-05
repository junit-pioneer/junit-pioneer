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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Root element of the IssueExtension report.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "issues" })
@XmlRootElement(name = "issueReport")
public class IssueReport {

	protected Issues issues;

	public IssueReport() {
		this.issues = new Issues();
	}

	public IssueReport(Issues issues) {
		this.issues = issues;
	}

	public Issues getIssues() {
		return issues;
	}

	public void setIssues(Issues issues) {
		this.issues = issues;
	}

}
