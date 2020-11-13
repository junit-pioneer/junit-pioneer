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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>This listener collects the names and results of all tests, which are annotated with the {@link org.junitpioneer.jupiter.Issue} annotation.
 * After all tests are finished the results are provided to an {@link IssueProcessor} for further processing.</p>
 */
public class IssueExtensionExecutionListener implements TestExecutionListener {

	public static final String REPORT_ENTRY_KEY = "Issue";

	// Storage with all tests that belong to an issue <issueId, List<UniqueIdentifier>>
	private final ConcurrentHashMap<String, List<String>> issueTestsStorage = new ConcurrentHashMap<>();

	// Storage with tests results of test cases <UniqueIdentifier, result>
	private final ConcurrentHashMap<String, String> testStatusStorage = new ConcurrentHashMap<>();

	// Package private by purpose for testing
	List<IssuedTestCase> allIssuedTests = new ArrayList<>();

	@Override
	public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
		String testId = testIdentifier.getUniqueId();

		// Check if the report entry is an issue id
		Map<String, String> entryKeyValues = entry.getKeyValuePairs();
		if (entryKeyValues.containsKey(REPORT_ENTRY_KEY)) {
			String issueId = entryKeyValues.get(REPORT_ENTRY_KEY);

			// Store that the current test belongs to annotated issue
			issueTestsStorage.computeIfAbsent(issueId, __ -> new ArrayList<>()).add(testId);
		}
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		if (testIdentifier.isTest()) {
			// Store test result in cache
			testStatusStorage.put(testIdentifier.getUniqueId(), testExecutionResult.getStatus().toString());
		}
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		// Retrieve all tests, which are annotated with @Issue, and their result.
		for (Map.Entry<String, List<String>> entry : issueTestsStorage.entrySet()) {
			String issueId = entry.getKey();
			List<String> allTests = entry.getValue();

			allTests.forEach(testID -> {
				String status = testStatusStorage.getOrDefault(testID, "UNKNOWN");

				allIssuedTests.add(new IssuedTestCase(testID, issueId, status));
			});
		}

		// Pass results to all IssueProcessors
		Iterator<IssueProcessor> processors = IssueProcessorProvider.getInstance().providers();

		while (processors.hasNext()) {
			processors.next().processTestResults(allIssuedTests);
		}
	}

}
