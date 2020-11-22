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

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.junit.platform.engine.TestExecutionResult.Status;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junitpioneer.jupiter.IssueProcessor;
import org.junitpioneer.jupiter.IssueTestSuite;

/**
 * <p>This listener collects the names and results of all tests, which are annotated with the {@link org.junitpioneer.jupiter.Issue @Issue} annotation.
 * After all tests are finished the results are provided to an {@link IssueProcessor} for further processing.</p>
 */
public class IssueExtensionExecutionListener implements TestExecutionListener {

	public static final String REPORT_ENTRY_KEY = "Issue";

	/**
	 * This listener will be active as soon as Pioneer is on the class/module path, regardless of whether {@code @Issue} is actually used.
	 * To prevent superfluous computation and memory use, we "deactivate" this listener if it is not needed.
	 * That's the case when we detect no {@code IssueProcessor} - presumably nobody uses this extension then.
	 */
	private final boolean active;
	private final ConcurrentMap<String, IssueTestCaseBuilder> testCases;

	public IssueExtensionExecutionListener() {
		this.active = ServiceLoader.load(IssueProcessor.class).iterator().hasNext();
		this.testCases = new ConcurrentHashMap<>();
	}

	// needed for tests to circumvent deactivation
	IssueExtensionExecutionListener(boolean active) {
		this.active = active;
		this.testCases = new ConcurrentHashMap<>();
	}

	@Override
	public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
		if (!active)
			return;

		String testId = testIdentifier.getUniqueId();
		Map<String, String> messages = entry.getKeyValuePairs();

		if (messages.containsKey(REPORT_ENTRY_KEY)) {
			String issueId = messages.get(REPORT_ENTRY_KEY);
			testCases.computeIfAbsent(testId, IssueTestCaseBuilder::new).setIssueId(issueId);
		}
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		if (!active)
			return;

		if (testIdentifier.isTest()) {
			String testId = testIdentifier.getUniqueId();
			Status status = testExecutionResult.getStatus();
			testCases.computeIfAbsent(testId, IssueTestCaseBuilder::new).setResult(status);
		}
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		if (!active)
			return;

		List<IssueTestSuite> issueTestSuites = createIssueTestSuites();
		for (IssueProcessor issueProcessor : ServiceLoader.load(IssueProcessor.class)) {
			issueProcessor.processTestResults(issueTestSuites);
		}
	}

	List<IssueTestSuite> createIssueTestSuites() {
		//@formatter:off
		List<IssueTestSuite> suites = testCases
				.values().stream()
				.collect(toMap(IssueTestCaseBuilder::getIssueId, Arrays::asList))
				.entrySet().stream()
				.map(issueIdWithTestCases -> new IssueTestSuite(
						issueIdWithTestCases.getKey(),
						issueIdWithTestCases
								.getValue().stream()
								.map(IssueTestCaseBuilder::build)
								.collect(toList())))
				.collect(toList());
		//@formatter:on
		return Collections.unmodifiableList(suites);
	}

}
