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

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>This listener collects the names and results of all tests, which are annotated with the "@Issue" annotation.
 * After all tests are finished the results are provided to the abstract IssueProcessor for further processing.</p>
 *
 * @see <a href="https://junit.org/junit5/docs/current/api/org.junit.platform.launcher/org/junit/platform/launcher/TestPlan.html">JUnit Docs</a>
 */
public class IssueExtensionListener implements TestExecutionListener {

	static final String KEY_ISSUE = "Issue";

	// Cache with all tests that belong to an issue <issueId, List<UniqueIdentifier>>
	ConcurrentHashMap<String, List<String>> issueTestsCache = new ConcurrentHashMap<>();

	// Cache with tests results of test cases <UniqueIdentifier, result>
	ConcurrentHashMap<String, String> testStatusCache = new ConcurrentHashMap<>();

	@Override
	public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
		String testId = testIdentifier.getUniqueId();

		// Check if the report entry is an issue id
		Map<String, String> entryKeyValues = entry.getKeyValuePairs();
		if (entryKeyValues.containsKey(KEY_ISSUE)) {
			String issueId = entryKeyValues.get(KEY_ISSUE);

			// Store that the current test belongs to issue
			issueTestsCache.putIfAbsent(issueId, new ArrayList<>());
			issueTestsCache.get(issueId).add(testId);

		}
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		if (testIdentifier.isTest()) {
			// Store test result in cache
			testStatusCache.put(testIdentifier.getUniqueId(), testExecutionResult.getStatus().toString());
		}
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		List<IssuedTestCase> allIssuedTests = new ArrayList<>();

		for (Map.Entry<String, List<String>> entry : issueTestsCache.entrySet()) {
			String issueId = entry.getKey();
			List<String> allTests = entry.getValue();

			allTests.forEach((testID) -> {
				String status = testStatusCache.getOrDefault(testID, "UNKNOWN");

				allIssuedTests.add(new IssuedTestCase(testID, issueId, status));
			});
		}

		// TODO Pass to abstract class
	}
}
