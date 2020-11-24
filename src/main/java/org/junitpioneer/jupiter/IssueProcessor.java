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

import java.util.List;

/**
 * This interfaces offers a method to process the results of `@Issue` annotated test cases.
 */
public interface IssueProcessor {

	/**
	 * Processes results of `@Issue` annotated test cases grouped by the issueId, called {@link IssueTestSuite}.
	 *
	 * @param issueTestSuites List of issues, each with a list of test cases annotated with their issueId
	 */
	void processTestResults(List<IssueTestSuite> issueTestSuites);

}
